package io.canis.jpaw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.canis.jpaw.client.Jpaw;
import io.canis.jpaw.client.JpawClient;
import io.canis.jpaw.utils.Parser;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


class JpawIntegrationTest {

  private static Jpaw canis;

  @BeforeAll
  static void serverStart() throws IOException {
    canis = new JpawClient();
  }

  @Test
  public void testLoginCommand() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(2);
    Exception[] exceptions = new Exception[2];
    AssertionError[] assertionErrors = new AssertionError[2];

    var t1 = new Thread(() -> {
      try {
        var str = Parser.parseString(canis.set("Bapi"));
        var map = canis.get("Bapi");
        assertEquals("OK", str);
        assertEquals("Bapi", map.get("name"));
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
        var str = Parser.parseString(canis.set("Ziggy"));
        var map = canis.get("Ziggy");
        assertEquals("OK", str);
        assertEquals("Ziggy", map.get("name"));
      } catch (AssertionError e) {
        assertionErrors[1] = e;
      } catch (IOException e) {
        exceptions[1] = e;
      } finally {
        latch.countDown();
      }
    });

    t1.start();
    t2.start();

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

  @Test
  void testHealthCommand() throws IOException {
    String result = canis.health();
    assertEquals("OK", Parser.parseString(result));
  }

  @Test
  void testAddCommand() throws IOException {
    String result = canis.set("Alice");
    assertEquals("OK", Parser.parseString(result));
  }

  @Test
  void testGetCommand() throws IOException {
    canis.set("Mikey");
    Map<String, Object> result = canis.get("Mikey");
    assertEquals("Mikey", result.get("name"));
    assertNotNull(result.get("publicKey"));
  }

  @Test
  void testDeleteCommand() throws IOException {
    boolean result = canis.delete("Alice");
    assertTrue(result);
  }
}

