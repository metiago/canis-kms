package io.canis.client;

import io.canis.client.parsers.MapParser;
import java.io.IOException;
import java.util.Map;

public class CanisClient implements Canis {

  private final SocketClient socketClient;

  public CanisClient() throws IOException {
    this.socketClient = new SocketClient("0.0.0.0", 3307, "admin", "123");
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
  public Map<String, Object> get(String key) throws IOException {
    String command = String.format("|get %s", key);
    var socketResp = this.socketClient.getString(command);
    return MapParser.parseMap(socketResp);
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

