package io.canis;

import io.canis.config.Environment;
import io.canis.config.TlsConfiguration;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
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
    private Path certificateAuthority;
    private Path certificateAuthorityTrust;

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
                directory.resolve("secret.key"),
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

    public Environment environmentTrustingCertificateAuthority() throws Exception {
        ensureCertificateAuthority();
        Environment original = environment();
        return new Environment(
                original.password(),
                original.username(),
                original.port(),
                original.serviceCredentials(),
                original.secretKeyPath(),
                new TlsConfiguration(serverIdentity, password, certificateAuthorityTrust, password));
    }

    public SSLContext validCaSignedClientContext() throws Exception {
        return caSignedClientContext("valid-client", null, "clientAuth");
    }

    public SSLContext expiredClientContext() throws Exception {
        return caSignedClientContext("expired-client", "2020/01/01 00:00:00", "clientAuth");
    }

    public SSLContext wrongUsageClientContext() throws Exception {
        return caSignedClientContext("wrong-usage-client", null, "serverAuth");
    }

    private SSLContext caSignedClientContext(String alias, String startDate, String extendedKeyUsage) throws Exception {
        ensureCertificateAuthority();
        Path identity = directory.resolve(alias + ".p12");
        generate(alias, identity, "client");
        Path request = directory.resolve(alias + ".csr");
        Path certificate = directory.resolve(alias + ".cer");
        run(
                "-certreq",
                "-alias",
                alias,
                "-keystore",
                identity.toString(),
                "-storetype",
                "PKCS12",
                "-storepass",
                password,
                "-file",
                request.toString());
        var issue = new java.util.ArrayList<String>(java.util.List.of(
                "-gencert",
                "-alias",
                "ca",
                "-keystore",
                certificateAuthority.toString(),
                "-storetype",
                "PKCS12",
                "-storepass",
                password,
                "-infile",
                request.toString(),
                "-outfile",
                certificate.toString(),
                "-validity",
                startDate == null ? "2" : "1",
                "-ext",
                "SAN=dns:client",
                "-ext",
                "EKU=" + extendedKeyUsage));
        if (startDate != null) {
            issue.add("-startdate");
            issue.add(startDate);
        }
        run(issue.toArray(String[]::new));
        importCertificate("ca", directory.resolve("ca.cer"), identity);
        importCertificate(alias, certificate, identity);
        verifyIssuedCertificate(identity, alias, startDate != null, extendedKeyUsage);
        return context(identity, clientTrust);
    }

    private void verifyIssuedCertificate(Path identity, String alias, boolean expired, String extendedKeyUsage)
            throws Exception {
        X509Certificate certificate = (X509Certificate) load(identity).getCertificate(alias);
        if (expired != certificate.getNotAfter().before(new Date())) {
            throw new IllegalStateException("TLS test certificate has the wrong validity period");
        }
        String expectedUsage = extendedKeyUsage.equals("serverAuth") ? "1.3.6.1.5.5.7.3.1" : "1.3.6.1.5.5.7.3.2";
        if (!java.util.List.of(expectedUsage).equals(certificate.getExtendedKeyUsage())) {
            throw new IllegalStateException("TLS test certificate has the wrong extended key usage");
        }
        certificate.verify(load(certificateAuthority).getCertificate("ca").getPublicKey());
    }

    private void ensureCertificateAuthority() throws Exception {
        if (certificateAuthority != null) {
            return;
        }
        certificateAuthority = directory.resolve("ca.p12");
        certificateAuthorityTrust = directory.resolve("ca-trust.p12");
        run(
                "-genkeypair",
                "-alias",
                "ca",
                "-keyalg",
                "RSA",
                "-keysize",
                "2048",
                "-validity",
                "30",
                "-dname",
                "CN=CANIS test CA",
                "-ext",
                "BC=ca:true",
                "-ext",
                "KU=keyCertSign,cRLSign",
                "-keystore",
                certificateAuthority.toString(),
                "-storetype",
                "PKCS12",
                "-storepass",
                password,
                "-keypass",
                password,
                "-noprompt");
        Path certificate = directory.resolve("ca.cer");
        run(
                "-exportcert",
                "-alias",
                "ca",
                "-keystore",
                certificateAuthority.toString(),
                "-storetype",
                "PKCS12",
                "-storepass",
                password,
                "-file",
                certificate.toString());
        importCertificate("ca", certificate, certificateAuthorityTrust);
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
