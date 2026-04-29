package io.canis.jpaw.pojo;

public class Environment {

  private String host;
  private int port;
  private String username;
  private String password;

  public Environment(String password, String username, String host, int port) {
    this.password = password;
    this.username = username;
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
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
