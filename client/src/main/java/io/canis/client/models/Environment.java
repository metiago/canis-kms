package io.canis.client.models;

public class Environment {

  private int port;
  private String username;
  private String password;

  public Environment(String password, String username, int port) {
    this.password = password;
    this.username = username;
    this.port = port;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
