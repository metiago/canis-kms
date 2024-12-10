package io.canis.client;

import java.io.IOException;

public class CanisClient implements Canis {

  private final SocketClient socketClient;

  public CanisClient() throws IOException {
    // TODO get data from env vars
    this.socketClient = new SocketClient("0.0.0.0", 3307, "admin", "admin123");
  }

  @Override
  public String health() throws IOException {
    String command = "|health";
    return this.socketClient.getString(command);
  }

  @Override
  public String set(String key) throws IOException {
    String command = String.format("|set %s", key);
    return this.socketClient.getString(command);
  }

  @Override
  public String get(String key) throws IOException {
    String command = String.format("|get %s", key);
    return this.socketClient.getString(command);
  }

  @Override
  public boolean delete(String key) throws IOException {
    String command = String.format("|del %s", key);
    this.socketClient.getString(command);
    return true;
  }

  @Override
  public String list() throws IOException {
    String command = "|list";
    return this.socketClient.getString(command);
  }

}

