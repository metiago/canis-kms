package io.canis.jpaw.pojo;

public record Environment(
        String host,
        int port,
        String username,
        String password
) {

  public Environment(String password, String username, String host, int port) {
    this(host, port, username, password);
  }
}
