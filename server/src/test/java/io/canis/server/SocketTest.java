package io.canis.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.canis.Server;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SocketTest {

  CountDownLatch latch = new CountDownLatch(3);
  Exception[] exceptions = new Exception[3];
  AssertionError[] assertionErrors = new AssertionError[3];

  @BeforeAll
  static void serverStart() throws IOException {
    new Thread(() -> new Server().start()).start();
  }

  @Test
  void testLoginOK() throws IOException {
    var resp = sendCommand(String.format("|login %s:%s", "admin", "123"));
    assertEquals("Authentication successful", resp);
  }

  @Test
  void testInvalidCommandError() throws IOException {
    var resp = sendCommand(String.format("|login"));
    assertEquals("Unknown command", resp);
  }

  @Test
  public void testMultipleThreadOK() throws InterruptedException {

    var t1 = new Thread(() -> {
      try {
        String resp = sendCommand("|health");
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
        String resp = sendCommand(String.format("|set %s", "appKey"));
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
        String resp = sendCommand(String.format("|get %s", "appKey"));
        assertTrue(resp.matches("\\|ms>.*"));
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
  }

  String sendCommand(String command) throws IOException {

    try (Socket socket = new Socket("0.0.0.0", Integer.parseInt(System.getenv("CANIS_PORT")));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream)) {

      out.println(command);

      int length = dataInputStream.readInt();
      byte[] bytes = new byte[length];
      dataInputStream.readFully(bytes);

      return new String(bytes, StandardCharsets.UTF_8);
    }
  }
}
