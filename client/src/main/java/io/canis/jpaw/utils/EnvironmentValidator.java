package io.canis.jpaw.utils;

import io.canis.jpaw.pojo.Environment;

public class EnvironmentValidator {

  public static Environment validateEnvironment() {
    var serverPort = System.getenv("CANIS_SERVER_PORT");
    var username = System.getenv("CANIS_USERNAME");
    var password = System.getenv("CANIS_PASSWORD");
    if (serverPort == null || username == null || password == null) {
      System.err.println("Environment variables not set. Please ensure to set CANIS_SERVER_PORT, CANIS_USERNAME and CANIS_PASSWORD");
      System.exit(1);
    }
    return new Environment(password, username, Integer.parseInt(serverPort));
  }

}
