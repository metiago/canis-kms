package io.canis.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.canis.Server;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SocketTest {

  CountDownLatch latch = new CountDownLatch(3);
  Exception[] exceptions = new Exception[3];
  AssertionError[] assertionErrors = new AssertionError[3];

  @BeforeAll
  static void serverStart() throws IOException, InterruptedException {
    assumeTrue(System.getenv("CANIS_PORT") != null, "CANIS_PORT must be set");
    assumeTrue(System.getenv("CANIS_USERNAME") != null, "CANIS_USERNAME must be set");
    assumeTrue(System.getenv("CANIS_PASSWORD") != null, "CANIS_PASSWORD must be set");
    assumeTrue(System.getenv("CANIS_SECRET_KEY") != null, "CANIS_SECRET_KEY must be set");

    Thread serverThread = new Thread(() -> new Server().start());
    serverThread.setDaemon(true);
    serverThread.start();
    TimeUnit.MILLISECONDS.sleep(100);
  }

  @Test
  void testLoginOK() throws IOException {
    var resp = sendCommand(String.format("|login %s:%s", username(), password()));
    assertEquals("Authentication successful", resp);
  }

  @Test
  void testInvalidCommandError() throws IOException {
    var resp = sendCommand("|health");
    assertEquals("Authentication failed", resp);
  }

  @Test
  void testMalformedLoginError() throws IOException {
    var resp = sendCommand("|login");
    assertEquals("Invalid input format", resp);
  }

  @Test
  public void testMultipleThreadOK() throws InterruptedException, IOException {

    var t1 = new Thread(() -> {
      try {
        String resp = sendAuthenticatedCommand("|health");
        assertEquals("|s>OK", resp);
      } catch (AssertionError e) {
        assertionErrors[0] = e;
      } catch (IOException e) {
        exceptions[0] = e;
      } finally {
        latch.countDown();
      }
    });

    var t2 = new Thread(() -> {
      try {
        String resp = sendAuthenticatedCommand(String.format("|set %s", "appKey"));
        assertEquals("|s>OK", resp);
      } catch (AssertionError e) {
        assertionErrors[1] = e;
      } catch (IOException e) {
        exceptions[1] = e;
      } finally {
        latch.countDown();
      }
    });

    var t3 = new Thread(() -> {
      try {
        String resp = sendAuthenticatedCommand(String.format("|set %s", "appKey2"));
        assertEquals("|s>OK", resp);
      } catch (AssertionError e) {
        assertionErrors[2] = e;
      } catch (IOException e) {
        exceptions[2] = e;
      } finally {
        latch.countDown();
      }
    });

    t1.start();
    t2.start();
    t3.start();

    latch.await();

    for (AssertionError error : assertionErrors) {
      if (error != null) {
        fail("Test failed: " + error.getMessage());
      }
    }

    for (Exception e : exceptions) {
      if (e != null) {
        fail("Test failed: " + e.getMessage());
      }
    }

    String resp = sendAuthenticatedCommand(String.format("|get %s", "appKey"));
    assertTrue(resp.matches("\\|ms>.*"));
  }

  String sendCommand(String command) throws IOException {

    try (Socket socket = new Socket("0.0.0.0", Integer.parseInt(System.getenv("CANIS_PORT")));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream)) {

      out.println(command);

      return readResponse(dataInputStream);
    }
  }

  String sendAuthenticatedCommand(String command) throws IOException {

    try (Socket socket = new Socket("0.0.0.0", Integer.parseInt(System.getenv("CANIS_PORT")));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream)) {

      out.println(String.format("|login %s:%s", username(), password()));
      assertEquals("Authentication successful", readResponse(dataInputStream));

      out.println(command);

      return readResponse(dataInputStream);
    }
  }

  String readResponse(DataInputStream dataInputStream) throws IOException {
    int length = dataInputStream.readInt();
    byte[] bytes = new byte[length];
    dataInputStream.readFully(bytes);

    return new String(bytes, StandardCharsets.UTF_8);
  }

  String username() {
    return System.getenv("CANIS_USERNAME");
  }

  String password() {
    return System.getenv("CANIS_PASSWORD");
  }
}
