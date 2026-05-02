package io.canis.store;

import io.canis.crypto.SymmetricGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyValueStore {

  private static final Logger logger = LoggerFactory.getLogger(KeyValueStore.class);
  private static final byte[] STORE_MAGIC = "CANISDB1".getBytes(StandardCharsets.US_ASCII);
  private static final int MAX_ENTRIES = 100_000;
  private Map<String, Entry> store;
  private final String filePath;
  private final String secretKeyPath;
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  public KeyValueStore() {
    this("db.dat", System.getenv("CANIS_SECRET_KEY"));
  }

  public KeyValueStore(String filePath, String secretKeyPath) {
    this.filePath = filePath;
    this.secretKeyPath = secretKeyPath;
    this.store = new ConcurrentHashMap<>();
    loadFromFile();
  }

  public void loadFromFile() {
    lock.writeLock().lock();
    try {
      File file = new File(filePath);
      if (file.exists()) {

        try (InputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

          byte[] buffer = new byte[1024];

          int bytesRead;
          while ((bytesRead = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
          }
          byte[] encryptedData = baos.toByteArray();
          byte[] decryptedData = symmetricGenerator().decrypt(encryptedData);
          store = deserializeVersionedStore(decryptedData);

        } catch (IOException e) {
          logger.error(e.getMessage(), e);
        }
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void saveToFile() {
    lock.writeLock().lock();
    try {

      byte[] serializedData = serializeStore(store);
      byte[] encryptedData = symmetricGenerator().encrypt(serializedData);
      try (OutputStream fos = new FileOutputStream(filePath)) {
        fos.write(encryptedData);
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void set(String key, Entry value) {
    lock.writeLock().lock();
    try {
      store.put(key, value);
      saveToFile();
    } finally {
      lock.writeLock().unlock();
    }
  }

  public Entry get(String key) {
    lock.readLock().lock();
    try {
      return store.get(key);
    } finally {
      lock.readLock().unlock();
    }
  }

  public void delete(String key) {
    lock.writeLock().lock();
    try {
      store.remove(key);
      saveToFile();
    } finally {
      lock.writeLock().unlock();
    }
  }

  public List<Entry> list() throws IOException {
    lock.readLock().lock();
    try {
      return new ArrayList<>(store.values());
    } finally {
      lock.readLock().unlock();
    }
  }

  private byte[] serializeStore(Map<String, Entry> map) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    try (DataOutputStream out = new DataOutputStream(byteStream)) {
      out.write(STORE_MAGIC);
      out.writeInt(map.size());
      for (Map.Entry<String, Entry> mapEntry : map.entrySet()) {
        Entry entry = mapEntry.getValue();
        writeString(out, mapEntry.getKey());
        writeString(out, entry.getName());
        writeString(out, entry.getPublicKey());
        writeString(out, entry.getPrivateKey());
      }
    }
    return byteStream.toByteArray();
  }

  private Map<String, Entry> deserializeVersionedStore(byte[] data) throws IOException {
    Map<String, Entry> result = new ConcurrentHashMap<>();
    try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
      byte[] magic = new byte[STORE_MAGIC.length];
      in.readFully(magic);
      if (!Arrays.equals(magic, STORE_MAGIC)) {
        throw new IOException("Invalid CANIS store format.");
      }

      int count = in.readInt();
      if (count < 0 || count > MAX_ENTRIES) {
        throw new IOException("Invalid CANIS store entry count: " + count);
      }

      for (int i = 0; i < count; i++) {
        String key = readString(in);
        String name = readString(in);
        String publicKey = readString(in);
        String privateKey = readString(in);
        result.put(key, new Entry(name, publicKey, privateKey));
      }
    }
    return result;
  }

  private void writeString(DataOutputStream out, String value) throws IOException {
    if (value == null) {
      out.writeInt(-1);
      return;
    }

    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    out.writeInt(bytes.length);
    out.write(bytes);
  }

  private String readString(DataInputStream in) throws IOException {
    int length = in.readInt();
    if (length < 0) {
      return null;
    }
    byte[] bytes = new byte[length];
    in.readFully(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private SymmetricGenerator symmetricGenerator() throws NoSuchAlgorithmException, IOException {
    return new SymmetricGenerator(secretKeyPath);
  }
}
