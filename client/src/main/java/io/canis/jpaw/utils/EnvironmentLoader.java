package io.canis.jpaw.utils;

import io.canis.jpaw.config.TlsConfiguration;
import io.canis.jpaw.pojo.Environment;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;

public final class EnvironmentLoader {

    private static final String DEFAULT_SERVER_HOST = "localhost";

    private EnvironmentLoader() {}

    public static Environment loadEnvironment() {
        return loadEnvironment(System.getenv());
    }

    static Environment loadEnvironment(Map<String, String> environment) {
        String serverPort = require(environment, "CANIS_SERVER_PORT");
        String serverHost = optional(environment);
        String username = require(environment, "CANIS_USERNAME");
        String password = require(environment, "CANIS_PASSWORD");
        TlsConfiguration tls = new TlsConfiguration(
                parsePath(require(environment, "CANIS_CLIENT_TLS_KEYSTORE")),
                require(environment, "CANIS_CLIENT_TLS_KEYSTORE_PASSWORD"),
                parsePath(require(environment, "CANIS_CLIENT_TLS_TRUSTSTORE")),
                require(environment, "CANIS_CLIENT_TLS_TRUSTSTORE_PASSWORD"));

        return new Environment(serverHost, parsePort(serverPort), username, password, tls);
    }

    private static String require(Map<String, String> environment, String name) {
        String value = environment.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Environment variable " + name + " is not set.");
        }
        return value;
    }

    private static String optional(Map<String, String> environment) {
        String value = environment.get("CANIS_SERVER_HOST");
        if (value == null || value.isBlank()) {
            return EnvironmentLoader.DEFAULT_SERVER_HOST;
        }
        return value;
    }

    private static int parsePort(String portEnv) {
        try {
            int port = Integer.parseInt(portEnv);
            if (port < 1 || port > 65535) {
                throw new IllegalStateException("Invalid port number: " + port + ". Port must be between 1 and 65535.");
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid port number: " + portEnv, e);
        }
    }

    private static Path parsePath(String value) {
        try {
            return Path.of(value);
        } catch (InvalidPathException e) {
            throw new IllegalStateException("Invalid client TLS store path");
        }
    }
}
