package io.canis.jpaw.client;

import io.canis.jpaw.config.TlsConfiguration;
import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
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
    private Path certificateAuthority;
    private Path certificateAuthorityTrust;

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
        return server(serverContext(trustClient));
    }

    SSLServerSocket serverWithExpiredCertificate() throws Exception {
        return serverWithCertificate("expired-server", "2020/01/01 00:00:00", "serverAuth");
    }

    SSLServerSocket serverWithValidCaCertificate() throws Exception {
        return serverWithCertificate("valid-server", null, "serverAuth");
    }

    SSLServerSocket serverWithWrongUsageCertificate() throws Exception {
        return serverWithCertificate("wrong-usage-server", null, "clientAuth");
    }

    TlsConfiguration certificateAuthorityTrustConfiguration() throws Exception {
        ensureCertificateAuthority();
        return new TlsConfiguration(clientIdentity, password, certificateAuthorityTrust, password);
    }

    private SSLServerSocket serverWithCertificate(String alias, String startDate, String extendedKeyUsage)
            throws Exception {
        ensureCertificateAuthority();
        Path identity = directory.resolve(alias + ".p12");
        generate(alias, identity, "localhost");
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
                "SAN=dns:localhost",
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
        return server(context(identity, clientIdentity));
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

    private SSLServerSocket server(SSLContext context) throws Exception {
        SSLServerSocket server =
                (SSLServerSocket) context.getServerSocketFactory().createServerSocket(0);
        server.setEnabledProtocols(new String[] {"TLSv1.3"});
        server.setNeedClientAuth(true);
        server.setSoTimeout(3000);
        return server;
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
        exportAndTrust("ca", certificateAuthority, certificateAuthorityTrust);
    }

    private void importCertificate(String alias, Path certificate, Path store) throws Exception {
        run(
                "-importcert",
                "-alias",
                alias,
                "-file",
                certificate.toString(),
                "-keystore",
                store.toString(),
                "-storetype",
                "PKCS12",
                "-storepass",
                password,
                "-noprompt");
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
