package io.canis;

import static io.canis.handlers.Commands.LOGIN;

import io.canis.handlers.ClientHandler;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  private final ExecutorService executorService;

  private static final int port = 3307;

  private static final Map<String, String> userDatabase = new HashMap<>();
  private static final Map<String, String> sessionStore = new ConcurrentHashMap<>();

  static {
    userDatabase.put("admin", "admin123");
    userDatabase.put("user1", "password1");
  }

  public Server() {
    this.executorService = Executors.newFixedThreadPool(6);
  }

  public void start() {
    validateEnvironment();
    try (ServerSocket serverSocket = new ServerSocket(getPort())) {
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

  private void validateEnvironment() {
    var secretKeyPath = System.getenv("CANIS_SECRET_KEY");
    if (secretKeyPath == null) {
      logger.error("Environment variable CANIS_SECRET_KEY is not set.");
      System.exit(1);
    }
  }

  private int getPort() {
    int port = 0;
    try {
      String portEnv = System.getenv("CANIS_PORT");
      if (portEnv == null) {
        logger.error("Environment variable CANIS_PORT is not set.");
        System.exit(1);
      }
      port = Integer.parseInt(portEnv);
      if (port < 1 || port > 65535) {
        System.err.println("Invalid port number: " + port + ". Port must be between 1 and 65535.");
        System.exit(1);
      }

    } catch (NumberFormatException e) {
      logger.error(e.getMessage(), e);
      System.exit(1);
    }
    return port;
  }

  private boolean isCredentialValid(String username, String password) {
    String storedPassword = userDatabase.get(username);
    return storedPassword != null && storedPassword.equals(password);
  }

  private void authenticate(Socket socket) throws IOException {

    try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

      String input = in.readLine();
      if (!input.startsWith(LOGIN)) {
        var resp = "Authentication failed".getBytes(StandardCharsets.UTF_8);
        out.writeInt(resp.length);
        out.write(resp);
        return;
      }

      String args = input.substring(6).trim();
      String[] parts = args.split(":");

      if (parts.length < 2) {
        var resp = "Invalid input format".getBytes(StandardCharsets.UTF_8);
        out.writeInt(resp.length);
        out.write(resp);
        socket.close();
      }

      String username = parts[0];
      String password = parts[1];

      if (isCredentialValid(username, password)) {
        sessionStore.put(socket.getInetAddress().getHostAddress(), username);
        var resp = "Authentication successful".getBytes(StandardCharsets.UTF_8);
        out.writeInt(resp.length);
        out.write(resp);
      } else {
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