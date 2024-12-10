package io.canis.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.canis.Server;
import io.canis.client.parsers.MapParser;
import io.canis.client.parsers.StringParser;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CanisClientTest {

  private static CanisClient canis;

  @BeforeAll
  static void serverStart() throws IOException {
    new Thread(() -> new Server().start()).start();
    canis = new CanisClient();
  }

  @Test
  public void testLoginCommand() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(2);
    Exception[] exceptions = new Exception[2];
    AssertionError[] assertionErrors = new AssertionError[2];

    var t1 = new Thread(() -> {
      try {
        var str = StringParser.parseString(canis.set("Bapi"));
        var map = MapParser.parseMap(canis.get("Bapi"));
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
        var str = StringParser.parseString(canis.set("Ziggy"));
        var map = MapParser.parseMap(canis.get("Ziggy"));
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
    assertEquals("OK", StringParser.parseString(result));
  }

  @Test
  void testAddCommand() throws IOException {
    String result = canis.set("Alice");
    assertEquals("OK", StringParser.parseString(result));
  }

  @Test
  void testGetCommand() throws IOException {
    canis.set("Mikey");
    String data = canis.get("Mikey");
    Map<String, Object> result = MapParser.parseMap(data);
    assertEquals("Mikey", result.get("name"));
    assertNotNull(result.get("publicKey"));
    assertNotNull(result.get("privateKey"));
  }

  @Test
  void testListCommand() throws IOException {
    canis.set("Daisy");
    String data = canis.list();
    List<Map<String, Object>> result = MapParser.parseArray(data);
    assertNotNull(result);
    assertTrue(!result.isEmpty());
  }

  @Test
  void testDeleteCommand() throws IOException {
    boolean result = canis.delete("Alice");
    assertTrue(result);
  }
}

