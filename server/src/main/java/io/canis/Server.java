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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

  private static final Map<String, String> sessionStore = new ConcurrentHashMap<>();

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
        if (sessionStore.containsKey(socket.getInetAddress().getHostAddress())) {
          executorService.submit(new ClientHandler(socket));
        } else {
          authenticate(socket);
        }
      }

    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    } finally {
      executorService.shutdown();
    }
  }

  private boolean isCredentialValid(String u, String p) {
    return this.username.equals(u) && this.password.equals(p);
  }

  private void authenticate(Socket socket) throws IOException {

    try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

      String input = in.readLine();
      if (!input.startsWith(LOGIN)) {
        logger.info("Authentication failed: {}", input);
        var resp = "Authentication failed".getBytes(StandardCharsets.UTF_8);
        out.writeInt(resp.length);
        out.write(resp);
        return;
      }

      String args = input.substring(6).trim();
      String[] parts = args.split(":");

      if (parts.length < 2) {
        logger.info("Invalid input format when authenticating: {}",
            socket.getRemoteSocketAddress());
        var resp = "Invalid input format".getBytes(StandardCharsets.UTF_8);
        out.writeInt(resp.length);
        out.write(resp);
        socket.close();
      }

      String username = parts[0];
      String password = parts[1];

      logger.info("Authenticating username: {}", username);
      if (isCredentialValid(username, password)) {
        logger.info("Authentication successful for username: {}", username);
        sessionStore.put(socket.getInetAddress().getHostAddress(), username);
        var resp = "Authentication successful".getBytes(StandardCharsets.UTF_8);
        out.writeInt(resp.length);
        out.write(resp);
      } else {
        logger.info("Authentication failed for username: {}", username);
        var resp = "Authentication failed".getBytes(StandardCharsets.UTF_8);
        out.writeInt(resp.length);
        out.write(resp);
      }
    }
  }

  public static void main(String[] args) {
    new Server().start();
  }
}