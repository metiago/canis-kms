package io.canis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.canis.config.Environment;
import io.canis.store.KeyValueStore;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServerEndToEndTest {

  @TempDir
  Path tempDir;

  @Test
  void testAuthenticatedCommandFlowAndPersistenceAcrossRestart() throws Exception {
    Path databasePath = tempDir.resolve("db.dat");
    Path secretKeyPath = tempDir.resolve("secret.key");

    Server firstServer = startServer(databasePath, secretKeyPath);
    try {
      int port = waitForPort(firstServer);

      assertTrue(sendAuthenticatedCommand(port, "|version").contains("CANISP/1"));
      assertTrue(sendAuthenticatedCommand(port, "|set serviceA").contains("OK"));

      String getResponse = sendAuthenticatedCommand(port, "|get serviceA");
      assertTrue(getResponse.contains("|ms>name:serviceA"));
      assertTrue(getResponse.contains("|ms>publicKey:"));
      assertFalse(getResponse.contains("privateKey"));

      String listResponse = sendAuthenticatedCommand(port, "|list");
      assertTrue(listResponse.contains("|ms>name:serviceA"));
    } finally {
      firstServer.stop();
    }

    Server restartedServer = startServer(databasePath, secretKeyPath);
    try {
      int port = waitForPort(restartedServer);

      String persistedResponse = sendAuthenticatedCommand(port, "|get serviceA");
      assertTrue(persistedResponse.contains("|ms>name:serviceA"));
      assertTrue(persistedResponse.contains("|ms>publicKey:"));

      assertTrue(sendAuthenticatedCommand(port, "|del serviceA").contains("OK"));
      String listAfterDelete = sendAuthenticatedCommand(port, "|list");
      assertFalse(listAfterDelete.contains("|ms>name:serviceA"));
    } finally {
      restartedServer.stop();
    }
  }

  private Server startServer(Path databasePath, Path secretKeyPath) {
    Environment environment = new Environment("123", "admin", 0, Map.of("admin", "123"));
    KeyValueStore store = new KeyValueStore(databasePath.toString(), secretKeyPath.toString());
    Server server = new Server(environment, store);
    Thread serverThread = new Thread(server::start);
    serverThread.setDaemon(true);
    serverThread.start();
    return server;
  }

  private int waitForPort(Server server) throws InterruptedException {
    long deadline = System.currentTimeMillis() + 3000;
    while (server.getPort() == 0 && System.currentTimeMillis() < deadline) {
      Thread.sleep(10);
    }
    return server.getPort();
  }

  private String sendAuthenticatedCommand(int port, String command) throws IOException {
    try (Socket socket = new Socket("localhost", port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        DataInputStream in = new DataInputStream(socket.getInputStream())) {

      out.println("|login admin:123");
      readResponse(in);
      out.println(command);
      return readResponse(in);
    }
  }

  private String readResponse(DataInputStream in) throws IOException {
    int length = in.readInt();
    byte[] payload = new byte[length];
    in.readFully(payload);
    return new String(payload, StandardCharsets.UTF_8);
  }
}
