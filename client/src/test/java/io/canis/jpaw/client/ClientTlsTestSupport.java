package io.canis.jpaw.client;

import io.canis.jpaw.config.TlsConfiguration;
import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.HexFormat;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

/** Creates short-lived test-only client and server certificates with the JDK keytool. */
final class ClientTlsTestSupport {
    private final Path directory;
    private final String password;
    private final Path clientIdentity;
    private final Path serverIdentity;
    private final Path clientTrust;
    private final Path serverTrust;

    ClientTlsTestSupport(Path directory) throws Exception {
        this.directory = directory;
        byte[] random = new byte[16];
        new SecureRandom().nextBytes(random);
        password = HexFormat.of().formatHex(random);
        clientIdentity = directory.resolve("client.p12");
        serverIdentity = directory.resolve("server.p12");
        clientTrust = directory.resolve("client-trust.p12");
        serverTrust = directory.resolve("server-trust.p12");
        generate("client", clientIdentity, "client");
        generate("server", serverIdentity, "localhost");
        generate("rogue", directory.resolve("rogue.p12"), "rogue");
        exportAndTrust("client", clientIdentity, serverTrust);
        exportAndTrust("server", serverIdentity, clientTrust);
    }

    TlsConfiguration clientConfiguration() {
        return new TlsConfiguration(clientIdentity, password, clientTrust, password);
    }

    TlsConfiguration wrongHostTrustConfiguration() {
        return new TlsConfiguration(clientIdentity, password, serverTrust, password);
    }

    SSLContext serverContext(boolean trustedClient) throws Exception {
        Path identity = trustedClient ? clientIdentity : directory.resolve("rogue.p12");
        return context(serverIdentity, identity);
    }

    SSLServerSocket server(boolean trustClient) throws Exception {
        SSLServerSocket server = (SSLServerSocket)
                serverContext(trustClient).getServerSocketFactory().createServerSocket(0);
        server.setEnabledProtocols(new String[] {"TLSv1.3"});
        server.setNeedClientAuth(true);
        server.setSoTimeout(3000);
        return server;
    }

    private SSLContext context(Path identityPath, Path trustPath) throws Exception {
        var identity = load(identityPath);
        var keys = javax.net.ssl.KeyManagerFactory.getInstance(javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm());
        keys.init(identity, password.toCharArray());
        var trustStore = load(trustPath);
        var trust =
                javax.net.ssl.TrustManagerFactory.getInstance(javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
        trust.init(trustStore);
        SSLContext context = SSLContext.getInstance("TLSv1.3");
        context.init(keys.getKeyManagers(), trust.getTrustManagers(), null);
        return context;
    }

    private java.security.KeyStore load(Path path) throws Exception {
        var store = java.security.KeyStore.getInstance("PKCS12");
        try (var input = java.nio.file.Files.newInputStream(path)) {
            store.load(input, password.toCharArray());
        }
        return store;
    }

    private void generate(String alias, Path store, String host) throws Exception {
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
                "CN=" + host,
                "-ext",
                "SAN=dns:" + host,
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

    private void exportAndTrust(String alias, Path identity, Path trust) throws Exception {
        Path certificate = directory.resolve(alias + ".cer");
        run(
                "-exportcert",
                "-alias",
                alias,
                "-keystore",
                identity.toString(),
                "-storetype",
                "PKCS12",
                "-storepass",
                password,
                "-file",
                certificate.toString());
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

    private void run(String... args) throws Exception {
        String executable = Path.of(
                        System.getProperty("java.home"),
                        "bin",
                        System.getProperty("os.name").startsWith("Windows") ? "keytool.exe" : "keytool")
                .toString();
        String[] command = new String[args.length + 1];
        command[0] = executable;
        System.arraycopy(args, 0, command, 1, args.length);
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        process.getInputStream().readAllBytes();
        if (process.waitFor() != 0) {
            throw new IOException("Could not generate temporary TLS test material");
        }
    }
}
