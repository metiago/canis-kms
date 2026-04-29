package io.canis.server.store;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


import io.canis.store.Entry;
import io.canis.store.KeyValueStore;
import io.canis.utils.SymmetricGenerator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class KeyValueStoreTest {

  private static final byte[] STORE_MAGIC = "CANISDB1".getBytes(StandardCharsets.US_ASCII);

  @TempDir
  Path tempDir;

  private KeyValueStore keyValueStore;

  @BeforeEach
  public void setUp() throws NoSuchAlgorithmException, IOException {
    keyValueStore = spy(new KeyValueStore());
    doNothing().when(keyValueStore).loadFromFile();
    doNothing().when(keyValueStore).saveToFile();
  }

  @Test
  public void testSetAndGet() {
    Entry entry = new Entry();
    entry.setName("Bob");
    entry.setPublicKey("public-key-123");
    entry.setPrivateKey("private-key-123");

    keyValueStore.set("user1", entry);

    Entry result = keyValueStore.get("user1");
    assertNotNull(result);
    assertEquals("Bob", result.getName());
    assertEquals("public-key-123", result.getPublicKey());
    assertEquals("private-key-123", result.getPrivateKey());
  }

  @Test
  public void testDelete() {

    Entry entry = new Entry();
    entry.setName("Alice");
    entry.setPublicKey("public-key-456");
    entry.setPrivateKey("private-key-456");

    keyValueStore.set("user2", entry);
    keyValueStore.delete("user2");

    assertNull(keyValueStore.get("user2"));
  }

  @Test
  public void testList() throws IOException {

    Entry entry1 = new Entry();
    entry1.setName("Alice");
    entry1.setPublicKey("public-key-456");
    entry1.setPrivateKey("private-key-456");

    Entry entry2 = new Entry();
    entry2.setName("Bob");
    entry2.setPublicKey("public-key-789");
    entry2.setPrivateKey("private-key-789");

    keyValueStore.set("user1", entry1);
    keyValueStore.set("user2", entry2);

    List<Entry> data = keyValueStore.list();

    assertNotNull(data);
    assertTrue(!data.isEmpty());
  }

  @Test
  public void testSaveToFile() {

    Entry entry = new Entry();
    entry.setName("Eve");
    entry.setPublicKey("public-key-999");
    entry.setPrivateKey("private-key-999");

    keyValueStore.set("user3", entry);

    verify(keyValueStore, times(1)).saveToFile();
  }

  @Test
  public void testConcurrency() throws InterruptedException, ExecutionException {

    Runnable task1 = () -> keyValueStore.set("ziggy",
        new Entry("Ziggy", "public-key-111", "private-key-111"));
    Runnable task2 = () -> keyValueStore.set("bapi",
        new Entry("Bapi", "public-key-222", "private-key-222"));
    Runnable task3 = () -> keyValueStore.set("tiago",
        new Entry("Tiago", "public-key-333", "private-key-333"));

    Thread thread1 = new Thread(task1);
    Thread thread2 = new Thread(task2);
    Thread thread3 = new Thread(task3);
    thread1.start();
    thread2.start();
    thread3.start();
    thread1.join();
    thread2.join();
    thread3.join();

    assertNotNull(keyValueStore.get("ziggy"));
    assertNotNull(keyValueStore.get("tiago"));
    assertNotNull(keyValueStore.get("bapi"));
  }

  @Test
  public void testPersistenceUsesVersionedStoreFormat() throws Exception {
    Path databasePath = tempDir.resolve("db.dat");
    Path secretKeyPath = tempDir.resolve("secret.key");

    KeyValueStore store = new KeyValueStore(databasePath.toString(), secretKeyPath.toString());
    store.set("serviceA", new Entry("serviceA", "public-key", "private-key"));

    KeyValueStore reloadedStore = new KeyValueStore(databasePath.toString(), secretKeyPath.toString());
    Entry result = reloadedStore.get("serviceA");

    assertNotNull(result);
    assertEquals("serviceA", result.getName());
    assertEquals("public-key", result.getPublicKey());
    assertEquals("private-key", result.getPrivateKey());

    byte[] encryptedStore = Files.readAllBytes(databasePath);
    byte[] decryptedStore = new SymmetricGenerator(secretKeyPath.toString()).decrypt(encryptedStore);
    assertArrayEquals(STORE_MAGIC, Arrays.copyOf(decryptedStore, STORE_MAGIC.length));
  }
}

