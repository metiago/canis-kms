package io.canis.config;

import java.nio.file.Path;
import java.util.Objects;

/** External TLS material required before the server can bind a port. */
public record TlsConfiguration(Path keyStore, String keyStorePassword, Path trustStore, String trustStorePassword) {

    public TlsConfiguration {
        Objects.requireNonNull(keyStore, "TLS identity store path is required");
        Objects.requireNonNull(trustStore, "TLS trust store path is required");
        if (keyStorePassword == null
                || keyStorePassword.isBlank()
                || trustStorePassword == null
                || trustStorePassword.isBlank()) {
            throw new IllegalArgumentException("TLS store passwords are required");
        }
    }
}
