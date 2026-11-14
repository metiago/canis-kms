package io.canis;

import io.canis.config.Environment;
import io.canis.config.TlsConfiguration;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

/** Generates temporary identities using the JDK keytool, without committed key material. */
public final class TlsTestSupport {
    private final Path directory;
    private final String password;
    private final Path serverIdentity;
    private final Path serverTrust;
    private final Path clientIdentity;
    private final Path clientTrust;

    public TlsTestSupport(Path directory) throws Exception {
        this.directory = directory;
        byte[] random = new byte[16];
        new SecureRandom().nextBytes(random);
        password = HexFormat.of().formatHex(random);
        serverIdentity = directory.resolve("server.p12");
        serverTrust = directory.resolve("server-trust.p12");
        clientIdentity = directory.resolve("client.p12");
        clientTrust = directory.resolve("client-trust.p12");
        generate("server", serverIdentity, "localhost");
        generate("client", clientIdentity, "client");
        Path serverCertificate = directory.resolve("server.cer");
        Path clientCertificate = directory.resolve("client.cer");
        run(
                "-exportcert",
                "-alias",
                "server",
                "-keystore",
                serverIdentity.toString(),
                "-storetype",
                "PKCS12",
                "-storepass",
                password,
                "-file",
                serverCertificate.toString());
        run(
                "-exportcert",
                "-alias",
                "client",
                "-keystore",
                clientIdentity.toString(),
                "-storetype",
                "PKCS12",
                "-storepass",
                password,
                "-file",
                clientCertificate.toString());
        importCertificate("server", serverCertificate, clientTrust);
        importCertificate("client", clientCertificate, serverTrust);
    }

    public Environment environment() {
        return new Environment(
                "123",
                "admin",
                0,
                Map.of("admin", "123"),
                new TlsConfiguration(serverIdentity, password, serverTrust, password));
    }

    public SSLContext clientContext() throws Exception {
        return context(clientIdentity, clientTrust);
    }

    public io.canis.jpaw.config.TlsConfiguration clientTlsConfiguration() {
        return new io.canis.jpaw.config.TlsConfiguration(clientIdentity, password, clientTrust, password);
    }

    public SSLContext untrustedClientContext() throws Exception {
        Path rogue = directory.resolve("rogue.p12");
        generate("rogue", rogue, "rogue");
        return context(rogue, clientTrust);
    }

    public SSLSocket connect(int port) throws Exception {
        SSLSocket socket = (SSLSocket) clientContext().getSocketFactory().createSocket("localhost", port);
        socket.setSoTimeout(3000);
        socket.setEnabledProtocols(new String[] {"TLSv1.3"});
        var parameters = socket.getSSLParameters();
        parameters.setEndpointIdentificationAlgorithm("HTTPS");
        socket.setSSLParameters(parameters);
        return socket;
    }

    private SSLContext context(Path identity, Path trustPath) throws Exception {
        KeyStore keys = load(identity);
        KeyManagerFactory keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagers.init(keys, password.toCharArray());
        TrustManagerFactory trustManagers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagers.init(load(trustPath));
        SSLContext context = SSLContext.getInstance("TLSv1.3");
        context.init(keyManagers.getKeyManagers(), trustManagers.getTrustManagers(), null);
        return context;
    }

    private KeyStore load(Path path) throws Exception {
        KeyStore store = KeyStore.getInstance("PKCS12");
        try (var input = java.nio.file.Files.newInputStream(path)) {
            store.load(input, password.toCharArray());
        }
        return store;
    }

    private void generate(String alias, Path store, String commonName) throws Exception {
        run(
                "-genkeypair",
                "-alias",
                alias,
                "-keyalg",
                "RSA",
                "-keysize",
                "2048",
                "-validity",
                "2",
                "-dname",
                "CN=" + commonName,
                "-ext",
                "SAN=dns:localhost,ip:127.0.0.1",
                "-keystore",
                store.toString(),
                "-storetype",
                "PKCS12",
                "-storepass",
                password,
                "-keypass",
                password,
                "-noprompt");
    }

    private void importCertificate(String alias, Path certificate, Path trust) throws Exception {
        run(
                "-importcert",
                "-alias",
                alias,
                "-file",
                certificate.toString(),
                "-keystore",
                trust.toString(),
                "-storetype",
                "PKCS12",
                "-storepass",
                password,
                "-noprompt");
    }

    private void run(String... arguments) throws Exception {
        String executable = Path.of(
                        System.getProperty("java.home"),
                        "bin",
                        System.getProperty("os.name").startsWith("Windows") ? "keytool.exe" : "keytool")
                .toString();
        String[] command = new String[arguments.length + 1];
        command[0] = executable;
        System.arraycopy(arguments, 0, command, 1, arguments.length);
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        process.getInputStream().readAllBytes();
        if (process.waitFor() != 0) {
            throw new IOException(
                    "Could not generate temporary TLS test material (keytool exit " + process.exitValue() + ")");
        }
    }
}
