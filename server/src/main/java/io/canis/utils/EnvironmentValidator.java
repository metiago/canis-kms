package io.canis.utils;

import io.canis.models.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentValidator {

  private static final Logger logger = LoggerFactory.getLogger(EnvironmentValidator.class);

  public static Environment getEnvironment() {
    var username = System.getenv("CANIS_USERNAME");
    var password = System.getenv("CANIS_PASSWORD");
    var secretKeyPath = System.getenv("CANIS_SECRET_KEY");
    if (secretKeyPath == null || username == null || password == null) {
      logger.error("Environment variables not set. Please ensure to set CANIS_USERNAME, CANIS_PASSWORD and CANIS_SECRET_KEY");
      System.exit(1);
    }
    return new Environment(password, username, getPort());
  }

  private static int getPort() {
    int port = 0;
    try {
      String portEnv = System.getenv("CANIS_PORT");
      if (portEnv == null) {
        logger.error("Environment variable CANIS_PORT is not set.");
        System.exit(1);
      }
      port = Integer.parseInt(portEnv);
      if (port < 1 || port > 65535) {
        System.err.println("Invalid port number: " + port + ". Port must be between 1 and 65535.");
        System.exit(1);
      }

    } catch (NumberFormatException e) {
      logger.error(e.getMessage(), e);
      System.exit(1);
    }
    return port;
  }

}
