package io.canis.client;

import static io.canis.client.utils.EnvironmentValidator.validateEnvironment;

import io.canis.client.models.Environment;
import io.canis.client.parsers.MapParser;
import java.io.IOException;
import java.util.Map;

public class CanisClient implements Canis {

  private final SocketClient socketClient;

  public CanisClient() throws IOException {
    Environment env = validateEnvironment();
    this.socketClient = new SocketClient("0.0.0.0", env.getPort(), env.getUsername(), env.getPassword());
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

