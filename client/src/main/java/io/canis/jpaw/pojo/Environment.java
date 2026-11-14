package io.canis.jpaw.pojo;

import io.canis.jpaw.config.TlsConfiguration;

public record Environment(String host, int port, String username, String password, TlsConfiguration tls) {

    public Environment {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Server host is required");
        }
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Client credentials are required");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid server port");
        }
        if (tls == null) {
            throw new IllegalArgumentException("TLS configuration is required");
        }
    }
}
