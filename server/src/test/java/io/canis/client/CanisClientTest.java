package io.canis.client;

import static io.canis.utils.AsymmetricPairGenerator.stringToPrivateKey;
import static io.canis.utils.AsymmetricPairGenerator.stringToPublicKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.canis.Server;
import io.canis.client.parsers.MapParser;
import io.canis.client.parsers.StringParser;
import io.canis.utils.FileDecryptor;
import io.canis.utils.FileEncryptor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CanisClientTest {

  private static final String INPUT_FILE_PATH = System.getProperty("user.dir") + "\\content.txt";
  private static final String ENCRYPTED_FILE_PATH = System.getProperty("user.dir") + "\\file.enc";
  private static final String DECRYPTED_FILE_PATH =
      System.getProperty("user.dir") + "\\dec_file.txt";
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
        var str = StringParser.parseString(canis.set("Ziggy"));
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
    Map<String, Object> result = canis.get("Mikey");

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

  @Test
  void testPrivateKeyEquals() throws IOException {
    canis.set("Mikey");
    Map<String, Object> result1 = canis.get("Mikey");
    Map<String, Object> result2 = canis.get("Mikey");

    assertEquals("Mikey", result1.get("name"));
    assertNotNull(result1.get("publicKey"));
    assertNotNull(result1.get("privateKey"));

    assertEquals(result1.get("publicKey"), result2.get("publicKey"));
  }

  @Test
  public void testEncryptDecryptFile() throws Exception {
    canis.set("order-ms");
    Map<String, Object> x = canis.get("order-ms");
    var publicKey = stringToPublicKey((String) x.get("publicKey"));
    var privateKey = stringToPrivateKey((String) x.get("privateKey"));

    String originalContent = "This is a test content for encryption.";
    writeToFile(INPUT_FILE_PATH, originalContent);

    File inputFile = new File(INPUT_FILE_PATH);
    File encryptedFile = new File(ENCRYPTED_FILE_PATH);
    FileEncryptor.encryptFile(inputFile, encryptedFile, publicKey);

    File decryptedFile = new File(DECRYPTED_FILE_PATH);
    FileDecryptor.decryptFile(encryptedFile, decryptedFile, privateKey);

    String decryptedContent = new String(Files.readAllBytes(decryptedFile.toPath()));
    assertEquals(originalContent, decryptedContent,"The decrypted content should match the original content.");
  }

  private void writeToFile(String filePath, String content) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
      writer.write(content);
    }
  }

  private void cleanUpFiles() {
    deleteFile(INPUT_FILE_PATH);
    deleteFile(ENCRYPTED_FILE_PATH);
    deleteFile(DECRYPTED_FILE_PATH);
  }

  private void deleteFile(String filePath) {
    File file = new File(filePath);
    if (file.exists()) {
      file.delete();
    }
  }

  @AfterEach
  public void tearDown() {
    cleanUpFiles();
  }
}

