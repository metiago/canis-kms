package io.canis.jpaw.config;

import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Arrays;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/** Creates a TLS 1.3 client context from externally supplied PKCS#12 stores. */
public final class TlsContextFactory {
    private TlsContextFactory() {}

    public static SSLContext create(TlsConfiguration config) {
        char[] keyPassword = config.keyStorePassword().toCharArray();
        char[] trustPassword = config.trustStorePassword().toCharArray();
        try {
            KeyStore identity = load(config.keyStore(), keyPassword);
            validateIdentity(identity, keyPassword);
            KeyStore trustedServers = load(config.trustStore(), trustPassword);
            validateTrustStore(trustedServers);

            KeyManagerFactory keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keys.init(identity, keyPassword);
            TrustManagerFactory trust = TrustManagerFactory.getInstance("PKIX");
            trust.init(trustedServers);
            SSLContext context = SSLContext.getInstance("TLSv1.3");
            context.init(keys.getKeyManagers(), trust.getTrustManagers(), null);
            return context;
        } catch (Exception e) {
            // Do not expose provider diagnostics, paths, or secret-bearing configuration.
            throw new IllegalStateException("Invalid client TLS configuration");
        } finally {
            Arrays.fill(keyPassword, '\0');
            Arrays.fill(trustPassword, '\0');
        }
    }

    private static KeyStore load(java.nio.file.Path path, char[] password) throws Exception {
        KeyStore store = KeyStore.getInstance("PKCS12");
        try (InputStream input = Files.newInputStream(path)) {
            store.load(input, password);
        }
        return store;
    }

    private static void validateIdentity(KeyStore store, char[] password) throws Exception {
        boolean found = false;
        for (var aliases = store.aliases(); aliases.hasMoreElements(); ) {
            String alias = aliases.nextElement();
            if (store.isKeyEntry(alias)) {
                if (found || !(store.getKey(alias, password) instanceof PrivateKey)) {
                    throw new IllegalStateException("Ambiguous client identity");
                }
                Certificate[] chain = store.getCertificateChain(alias);
                if (chain == null || chain.length == 0) {
                    throw new IllegalStateException("Missing client certificate chain");
                }
                for (Certificate certificate : chain) {
                    if (!(certificate instanceof X509Certificate x509)) {
                        throw new IllegalStateException("Invalid client certificate chain");
                    }
                    x509.checkValidity(java.util.Date.from(Instant.now()));
                }
                found = true;
            }
        }
        if (!found) {
            throw new IllegalStateException("No usable client identity");
        }
    }

    private static void validateTrustStore(KeyStore store) throws Exception {
        boolean found = false;
        for (var aliases = store.aliases(); aliases.hasMoreElements(); ) {
            String alias = aliases.nextElement();
            if (store.isCertificateEntry(alias) && store.getCertificate(alias) instanceof X509Certificate certificate) {
                certificate.checkValidity(java.util.Date.from(Instant.now()));
                found = true;
            }
        }
        if (!found) {
            throw new IllegalStateException("No usable trusted server certificate");
        }
    }
}
