package io.canis.jpaw.config;

import java.nio.file.Path;

/** External client identity and server trust material required for mutual TLS. */
public record TlsConfiguration(Path keyStore, String keyStorePassword, Path trustStore, String trustStorePassword) {
    public TlsConfiguration {
        if (keyStore == null || trustStore == null) {
            throw new IllegalArgumentException("TLS identity and trust store paths are required");
        }
        if (keyStorePassword == null
                || keyStorePassword.isBlank()
                || trustStorePassword == null
                || trustStorePassword.isBlank()) {
            throw new IllegalArgumentException("TLS store passwords are required");
        }
    }
}
