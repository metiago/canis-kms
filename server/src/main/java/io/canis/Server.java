package io.canis;

import static io.canis.handlers.Commands.LOGIN;
import static io.canis.utils.EnvironmentValidator.getEnvironment;

import io.canis.handlers.ClientHandler;
import io.canis.models.Environment;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  private final ExecutorService executorService;

  private final int port;
  private final String username;
  private final String password;

  public Server() {

    Environment env = getEnvironment();
    this.port = env.getPort();
    this.username = env.getUsername();
    this.password = env.getPassword();

    this.executorService = Executors.newFixedThreadPool(6);
  }

  public void start() {

    try (ServerSocket serverSocket = new ServerSocket(this.port)) {
      logger.info("Server is listening on port {}", port);
      while (true) {
        Socket socket = serverSocket.accept();
        executorService.submit(() -> handleConnection(socket));
      }

    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    } finally {
      executorService.shutdown();
    }
  }

  private void handleConnection(Socket socket) {
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());

      if (authenticate(socket, in, out)) {
        new ClientHandler(socket, in, out).run();
      } else {
        socket.close();
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      closeQuietly(socket);
    }
  }

  private boolean authenticate(Socket socket, BufferedReader in, DataOutputStream out)
      throws IOException {

    String input = in.readLine();
    if (input == null || !input.startsWith(LOGIN)) {
      logger.info("Authentication failed: {}", input);
      sendResponse(out, "Authentication failed");
      return false;
    }

    String args = input.substring(LOGIN.length()).trim();
    String[] parts = args.split(":", 2);

    if (parts.length < 2) {
      logger.info("Invalid input format when authenticating: {}", socket.getRemoteSocketAddress());
      sendResponse(out, "Invalid input format");
      return false;
    }

    String username = parts[0];
    String password = parts[1];

    logger.info("Authenticating username: {}", username);
    if (isCredentialValid(username, password)) {
      logger.info("Authentication successful for username: {}", username);
      sendResponse(out, "Authentication successful");
      return true;
    }

    logger.info("Authentication failed for username: {}", username);
    sendResponse(out, "Authentication failed");
    return false;
  }

  private void sendResponse(DataOutputStream out, String response) throws IOException {
    var resp = response.getBytes(StandardCharsets.UTF_8);
    out.writeInt(resp.length);
    out.write(resp);
  }

  private boolean isCredentialValid(String u, String p) {
    return this.username.equals(u) && this.password.equals(p);
  }

  private void closeQuietly(Socket socket) {
    try {
      socket.close();
    } catch (IOException e) {
      logger.warn(e.getMessage(), e);
    }
  }

  public static void main(String[] args) {
    new Server().start();
  }
}
