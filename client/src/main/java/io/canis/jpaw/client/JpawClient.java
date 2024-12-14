package io.canis.jpaw.client;

import static io.canis.jpaw.utils.EnvironmentValidator.validateEnvironment;

import io.canis.jpaw.pojo.Environment;
import io.canis.jpaw.utils.Parser;
import java.io.IOException;
import java.util.Map;

public class JpawClient implements Jpaw {

  private final SocketClient socketClient;

  public JpawClient() throws IOException {
    Environment env = validateEnvironment();
    this.socketClient = new SocketClient("0.0.0.0", env.getPort(), env.getUsername(), env.getPassword());
  }

  @Override
  public String health() throws IOException {
    String command = "|health";
    return this.socketClient.sendCommand(command);
  }

  @Override
  public String set(String key) throws IOException {
    String command = String.format("|set %s", key);
    return this.socketClient.sendCommand(command);
  }

  @Override
  public Map<String, Object> get(String key) throws IOException {
    String command = String.format("|get %s", key);
    var socketResp = this.socketClient.sendCommand(command);
    return Parser.parseMap(socketResp);
  }

  @Override
  public boolean delete(String key) throws IOException {
    String command = String.format("|del %s", key);
    this.socketClient.sendCommand(command);
    return true;
  }

}

