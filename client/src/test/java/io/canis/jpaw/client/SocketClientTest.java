package io.canis.jpaw.client;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class SocketClientTest {

  private static final int TOO_LARGE_RESPONSE_BYTES = 1_048_577;
  private static final String AUTH_SUCCESS = "Authentication successful";

  @Test
  void testRejectsOversizedResponseBeforeAllocation() throws Exception {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      Thread serverThread = new Thread(() -> sendOversizedAuthResponse(serverSocket));
      serverThread.start();

      assertThrows(IOException.class,
          () -> new SocketClient("localhost", serverSocket.getLocalPort(), "admin", "123"));

      serverThread.join(1000);
    }
  }

  @Test
  void testRejectsOversizedCommandBeforeSending() throws Exception {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      Thread serverThread = new Thread(() -> authenticateAndWaitForClose(serverSocket));
      serverThread.start();

      SocketClient client =
          new SocketClient("localhost", serverSocket.getLocalPort(), "admin", "123");
      try {
        assertThrows(IOException.class, () -> client.sendCommand("x".repeat(1_048_577)));
      } finally {
        client.close();
      }

      serverThread.join(1000);
    }
  }

  private void sendOversizedAuthResponse(ServerSocket serverSocket) {
    try (Socket socket = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

      in.readLine();
      out.writeInt(TOO_LARGE_RESPONSE_BYTES);
      out.flush();
    } catch (IOException ignored) {
    }
  }

  private void authenticateAndWaitForClose(ServerSocket serverSocket) {
    try (Socket socket = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

      in.readLine();
      byte[] authResponse = AUTH_SUCCESS.getBytes(StandardCharsets.UTF_8);
      out.writeInt(authResponse.length);
      out.write(authResponse);
      out.flush();
      in.readLine();
    } catch (IOException ignored) {
    }
  }
}
