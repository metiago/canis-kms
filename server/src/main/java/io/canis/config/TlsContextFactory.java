package io.canis.config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Arrays;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Builds a server context from externally supplied PKCS#12 stores. */
public final class TlsContextFactory {
  private static final Logger logger = LoggerFactory.getLogger(TlsContextFactory.class);

  private TlsContextFactory() {
  }

  public static SSLContext create(TlsConfiguration config) {
    char[] keyPassword = config.keyStorePassword().toCharArray();
    char[] trustPassword = config.trustStorePassword().toCharArray();
    try {
      KeyStore identity = load(config.keyStore(), keyPassword);
      validateIdentity(identity, keyPassword);
      KeyStore trustedClients = load(config.trustStore(), trustPassword);
      validateTrustStore(trustedClients);

      KeyManagerFactory keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keys.init(identity, keyPassword);
      TrustManagerFactory trust = TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm());
      trust.init(trustedClients);
      SSLContext context = SSLContext.getInstance("TLSv1.3");
      context.init(keys.getKeyManagers(), trust.getTrustManagers(), null);
      return context;
    } catch (Exception e) {
      // Provider exception messages can contain paths or other secrets.
      String category = failureCategory(e);
      logger.error("TLS configuration rejected: {}", category);
      throw new IllegalStateException("Invalid server TLS configuration (" + category + ")");
    } finally {
      Arrays.fill(keyPassword, '\0');
      Arrays.fill(trustPassword, '\0');
    }
  }

  private static KeyStore load(Path path, char[] password) throws Exception {
    KeyStore store = KeyStore.getInstance("PKCS12");
    try (InputStream input = Files.newInputStream(path)) {
      store.load(input, password);
    }
    return store;
  }

  private static void validateIdentity(KeyStore store, char[] password) throws Exception {
    boolean found = false;
    for (var aliases = store.aliases(); aliases.hasMoreElements();) {
      String alias = aliases.nextElement();
      if (store.isKeyEntry(alias)) {
        if (found || !(store.getKey(alias, password) instanceof PrivateKey)) {
          throw new GeneralSecurityException("Ambiguous or unusable server identity");
        }
        Certificate[] chain = store.getCertificateChain(alias);
        if (chain == null || chain.length == 0) {
          throw new GeneralSecurityException("Missing server certificate chain");
        }
        for (Certificate certificate : chain) {
          if (!(certificate instanceof X509Certificate x509)) {
            throw new GeneralSecurityException("Invalid server certificate chain");
          }
          x509.checkValidity(java.util.Date.from(Instant.now()));
        }
        found = true;
      }
    }
    if (!found) {
      throw new GeneralSecurityException("No usable server identity");
    }
  }

  private static void validateTrustStore(KeyStore store) throws Exception {
    boolean found = false;
    for (var aliases = store.aliases(); aliases.hasMoreElements();) {
      String alias = aliases.nextElement();
      if (store.isCertificateEntry(alias)
          && store.getCertificate(alias) instanceof X509Certificate certificate) {
        certificate.checkValidity(java.util.Date.from(Instant.now()));
        found = true;
      }
    }
    if (!found) {
      throw new GeneralSecurityException("No usable trusted client certificate");
    }
  }

  private static String failureCategory(Exception e) {
    if (e instanceof java.nio.file.NoSuchFileException) {
      return "store file missing";
    }
    if (e instanceof java.io.IOException) {
      return "store unreadable or password invalid";
    }
    if (e instanceof java.security.cert.CertificateExpiredException
        || e instanceof java.security.cert.CertificateNotYetValidException) {
      return "certificate outside validity period";
    }
    return "store or certificate invalid";
  }
}
