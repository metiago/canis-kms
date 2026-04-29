package io.canis.jpaw.client;

import static io.canis.jpaw.utils.EnvironmentLoader.loadEnvironment;

import io.canis.jpaw.pojo.Environment;
import io.canis.jpaw.utils.Parser;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class JpawClient implements Jpaw, AutoCloseable {

  private final SocketClient socketClient;

  public JpawClient() throws IOException {
    Environment env = loadEnvironment();
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
  public List<Map<String, Object>> list() throws IOException {
    String command = "|list";
    var socketResp = this.socketClient.sendCommand(command);
    return Parser.parseArray(socketResp);
  }

  @Override
  public byte[] decrypt(String key, byte[] encryptedData) throws IOException {
    String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData);
    String command = String.format("|decrypt %s %s", key, encryptedBase64);
    String response = Parser.parseString(this.socketClient.sendCommand(command));

    if (response.startsWith("ERROR:")) {
      throw new IOException(response);
    }

    return Base64.getDecoder().decode(response);
  }

  @Override
  public boolean delete(String key) throws IOException {
    String command = String.format("|del %s", key);
    this.socketClient.sendCommand(command);
    return true;
  }

  @Override
  public void close() throws IOException {
    this.socketClient.close();
  }

}
