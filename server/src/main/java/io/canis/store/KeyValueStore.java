package io.canis.store;

import io.canis.utils.SymmetricKeyGenerator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyValueStore {

  private static final Logger logger = LoggerFactory.getLogger(KeyValueStore.class);
  private Map<String, Entry> store;
  private final String filePath = "db.dat";
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  public KeyValueStore() throws NoSuchAlgorithmException, IOException {
    this.store = new ConcurrentHashMap<>();
    loadFromFile();
  }

  public void loadFromFile() {
    lock.readLock().lock();
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
          byte[] decryptedData = new SymmetricKeyGenerator().decrypt(encryptedData);
          try (ObjectInputStream ois = new ObjectInputStream(
              new ByteArrayInputStream(decryptedData))) {
            store = (Map<String, Entry>) ois.readObject();
          }
        } catch (IOException | ClassNotFoundException e) {
          logger.error(e.getMessage(), e);
        }
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    } finally {
      lock.readLock().unlock();
    }
  }

  public void saveToFile() {
    lock.writeLock().lock();
    try {
      byte[] serializedData = serializeMapToBytes(store);
      byte[] encryptedData = new SymmetricKeyGenerator().encrypt(serializedData);
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

  private byte[] serializeMapToBytes(Map<String, Entry> map) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
      objectStream.writeObject(map);
    }
    return byteStream.toByteArray();
  }
}
