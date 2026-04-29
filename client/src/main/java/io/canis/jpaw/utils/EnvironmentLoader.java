package io.canis.jpaw.utils;

import io.canis.jpaw.pojo.Environment;
import java.util.Map;

public final class EnvironmentLoader {

  private static final String DEFAULT_SERVER_HOST = "localhost";

  private EnvironmentLoader() {
  }

  public static Environment loadEnvironment() {
    return loadEnvironment(System.getenv());
  }

  static Environment loadEnvironment(Map<String, String> environment) {
    String serverPort = require(environment, "CANIS_SERVER_PORT");
    String serverHost = optional(environment, "CANIS_SERVER_HOST", DEFAULT_SERVER_HOST);
    String username = require(environment, "CANIS_USERNAME");
    String password = require(environment, "CANIS_PASSWORD");

    return new Environment(password, username, serverHost, parsePort(serverPort));
  }

  private static String require(Map<String, String> environment, String name) {
    String value = environment.get(name);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Environment variable " + name + " is not set.");
    }
    return value;
  }

  private static String optional(Map<String, String> environment, String name, String defaultValue) {
    String value = environment.get(name);
    if (value == null || value.isBlank()) {
      return defaultValue;
    }
    return value;
  }

  private static int parsePort(String portEnv) {
    try {
      int port = Integer.parseInt(portEnv);
      if (port < 1 || port > 65535) {
        throw new IllegalStateException(
            "Invalid port number: " + port + ". Port must be between 1 and 65535.");
      }
      return port;
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Invalid port number: " + portEnv, e);
    }
  }
}
