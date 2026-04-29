package io.canis.utils;

import io.canis.models.Environment;
import java.util.HashMap;
import java.util.Map;

public final class EnvironmentLoader {

  private EnvironmentLoader() {
  }

  public static Environment loadEnvironment() {
    return loadEnvironment(System.getenv());
  }

  static Environment loadEnvironment(Map<String, String> environment) {
    String username = require(environment, "CANIS_USERNAME");
    String password = require(environment, "CANIS_PASSWORD");
    require(environment, "CANIS_SECRET_KEY");
    int port = parsePort(require(environment, "CANIS_PORT"));
    Map<String, String> serviceCredentials = loadServiceCredentials(environment, username, password);

    return new Environment(password, username, port, serviceCredentials);
  }

  private static String require(Map<String, String> environment, String name) {
    String value = environment.get(name);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Environment variable " + name + " is not set.");
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

  private static Map<String, String> loadServiceCredentials(
      Map<String, String> environment, String username, String password) {

    Map<String, String> credentials = new HashMap<>();
    credentials.put(username, password);

    String serviceCredentials = environment.get("CANIS_SERVICE_CREDENTIALS");
    if (serviceCredentials == null || serviceCredentials.isBlank()) {
      return Map.copyOf(credentials);
    }

    String[] entries = serviceCredentials.split(",");
    for (String entry : entries) {
      String trimmed = entry.trim();
      if (trimmed.isEmpty()) {
        continue;
      }

      int separator = trimmed.indexOf(':');
      if (separator < 0) {
        throw new IllegalStateException("Invalid CANIS_SERVICE_CREDENTIALS entry: " + trimmed);
      }

      String serviceName = trimmed.substring(0, separator).trim();
      String servicePassword = trimmed.substring(separator + 1).trim();
      if (serviceName.isEmpty() || servicePassword.isEmpty()) {
        throw new IllegalStateException("Invalid CANIS_SERVICE_CREDENTIALS entry: " + trimmed);
      }

      credentials.put(serviceName, servicePassword);
    }

    return Map.copyOf(credentials);
  }
}
