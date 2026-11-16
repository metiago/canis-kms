package io.canis.config;

import java.nio.file.Path;
import java.util.Map;

public record Environment(
        String password,
        String username,
        int port,
        Map<String, String> serviceCredentials,
        Path secretKeyPath,
        TlsConfiguration tls) {}
