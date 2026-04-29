package io.canis;

import static io.canis.handlers.Commands.LOGIN;
import static io.canis.utils.EnvironmentLoader.loadEnvironment;

import io.canis.handlers.ClientHandler;
import io.canis.models.Environment;
import io.canis.models.LoginCredentials;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
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

    Environment env = loadEnvironment();
    this.port = env.port();
    this.username = env.username();
    this.password = env.password();

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
    if (input == null || !isLoginCommand(input)) {
      logger.info("Authentication failed: {}", input);
      sendResponse(out, "Authentication failed");
      return false;
    }

    Optional<LoginCredentials> credentials = parseLoginCredentials(input);
    if (credentials.isEmpty()) {
      logger.info("Invalid input format when authenticating: {}", socket.getRemoteSocketAddress());
      sendResponse(out, "Invalid input format");
      return false;
    }

    String username = credentials.get().username();
    String password = credentials.get().password();

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

  static Optional<LoginCredentials> parseLoginCredentials(String input) {
    if (!isLoginCommand(input)) {
      return Optional.empty();
    }

    String args = input.substring(LOGIN.length()).trim();
    int separator = args.indexOf(':');
    if (separator < 0) {
      return Optional.empty();
    }

    String username = args.substring(0, separator).trim();
    String password = args.substring(separator + 1).trim();
    if (username.isEmpty() || password.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new LoginCredentials(username, password));
  }

  private static boolean isLoginCommand(String input) {
    return input != null && (input.equals(LOGIN) || input.startsWith(LOGIN + " "));
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
