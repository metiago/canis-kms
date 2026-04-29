package io.canis;

import static io.canis.handlers.Commands.LOGIN;
import static io.canis.utils.EnvironmentLoader.loadEnvironment;

import io.canis.handlers.ClientHandler;
import io.canis.models.Environment;
import io.canis.models.LoginCredentials;
import io.canis.store.KeyValueStore;
import io.canis.utils.AuditLogger;
import io.canis.utils.BoundedLineReader;
import io.canis.utils.BoundedLineReader.LineTooLongException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

  private static final Logger logger = LoggerFactory.getLogger(Server.class);
  private static final int SOCKET_TIMEOUT_MS = 30_000;
  private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;

  private final ExecutorService executorService;

  private final int port;
  private final Map<String, String> serviceCredentials;
  private final KeyValueStore store;
  private volatile boolean running;
  private volatile ServerSocket serverSocket;

  public Server() {
    this(loadEnvironment(), new KeyValueStore());
  }

  Server(Environment env, KeyValueStore store) {
    this.port = env.port();
    this.serviceCredentials = env.serviceCredentials();
    this.store = store;
    this.executorService = Executors.newFixedThreadPool(6);
  }

  public void start() {

    running = true;
    try (ServerSocket boundServerSocket = new ServerSocket(this.port)) {
      this.serverSocket = boundServerSocket;
      logger.info("Server is listening on port {}", port);
      while (running) {
        try {
          Socket socket = boundServerSocket.accept();
          executorService.submit(() -> handleConnection(socket));
        } catch (SocketException e) {
          if (running) {
            logger.error(e.getMessage(), e);
          }
          break;
        }
      }

    } catch (IOException e) {
      if (running) {
        logger.error(e.getMessage(), e);
      }
    } finally {
      running = false;
      serverSocket = null;
      shutdownExecutor();
    }
  }

  public void stop() {
    running = false;
    ServerSocket currentServerSocket = serverSocket;
    if (currentServerSocket != null) {
      try {
        currentServerSocket.close();
      } catch (IOException e) {
        logger.warn(e.getMessage(), e);
      }
    }
  }

  public int getPort() {
    ServerSocket currentServerSocket = serverSocket;
    if (currentServerSocket == null) {
      return port;
    }
    return currentServerSocket.getLocalPort();
  }

  private void handleConnection(Socket socket) {
    try {
      socket.setSoTimeout(SOCKET_TIMEOUT_MS);
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());

      Optional<String> serviceIdentity = authenticate(socket, in, out);
      if (serviceIdentity.isPresent()) {
        new ClientHandler(socket, in, out, store, serviceIdentity.get()).run();
      } else {
        socket.close();
      }
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      closeQuietly(socket);
    }
  }

  private Optional<String> authenticate(Socket socket, BufferedReader in, DataOutputStream out)
      throws IOException {

    String input;
    try {
      input = BoundedLineReader.readLine(in);
    } catch (LineTooLongException e) {
      logger.warn("Authentication request exceeded maximum size: {}", socket.getRemoteSocketAddress());
      AuditLogger.authenticationFailed("unknown", socket.getRemoteSocketAddress(), "request_too_large");
      sendResponse(out, "Request too large");
      return Optional.empty();
    }

    if (input == null || !isLoginCommand(input)) {
      logger.info("Authentication failed: missing or invalid login command");
      AuditLogger.authenticationFailed("unknown", socket.getRemoteSocketAddress(), "missing_login");
      sendResponse(out, "Authentication failed");
      return Optional.empty();
    }

    Optional<LoginCredentials> credentials = parseLoginCredentials(input);
    if (credentials.isEmpty()) {
      logger.info("Invalid input format when authenticating: {}", socket.getRemoteSocketAddress());
      AuditLogger.authenticationFailed("unknown", socket.getRemoteSocketAddress(), "invalid_login_format");
      sendResponse(out, "Invalid input format");
      return Optional.empty();
    }

    String username = credentials.get().username();
    String password = credentials.get().password();

    logger.info("Authenticating username: {}", username);
    if (isCredentialValid(username, password)) {
      logger.info("Authentication successful for username: {}", username);
      AuditLogger.authenticationSucceeded(username, socket.getRemoteSocketAddress());
      sendResponse(out, "Authentication successful");
      return Optional.of(username);
    }

    logger.info("Authentication failed for username: {}", username);
    AuditLogger.authenticationFailed(username, socket.getRemoteSocketAddress(), "invalid_credentials");
    sendResponse(out, "Authentication failed");
    return Optional.empty();
  }

  private void sendResponse(DataOutputStream out, String response) throws IOException {
    var resp = response.getBytes(StandardCharsets.UTF_8);
    out.writeInt(resp.length);
    out.write(resp);
  }

  private boolean isCredentialValid(String u, String p) {
    return p.equals(this.serviceCredentials.get(u));
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

  private void shutdownExecutor() {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      executorService.shutdownNow();
    }
  }

  public static void main(String[] args) {
    new Server().start();
  }
}
