package io.canis.store;

import io.canis.crypto.Cryptographer;
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
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyValueStore {

    private static final Logger logger = LoggerFactory.getLogger(KeyValueStore.class);
    private static final byte[] STORE_MAGIC = "CANISDB1".getBytes(StandardCharsets.US_ASCII);
    private static final int MAX_ENTRIES = 100_000;
    private Map<String, StoredKey> store;
    private final String filePath;
    private final SymmetricGenerator symmetricGenerator;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public KeyValueStore() {
        this("db.dat", System.getenv("CANIS_SECRET_KEY"));
    }

    public KeyValueStore(String filePath, String secretKeyPath) {
        this.filePath = filePath;
        this.symmetricGenerator = new SymmetricGenerator(secretKeyPath);
        this.store = new ConcurrentHashMap<>();
        loadFromFile();
    }

    private void loadFromFile() {
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
                    byte[] decryptedData = symmetricGenerator.decrypt(encryptedData);
                    store = deserializeVersionedStore(decryptedData);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Unable to load the existing CANIS database. Verify that CANIS_SECRET_KEY points "
                                    + "to the original valid key file; the database was left unchanged.");
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void saveToFile() {
        try {
            byte[] serializedData = serializeStore(store);
            byte[] encryptedData = symmetricGenerator.encrypt(serializedData);
            try (OutputStream fos = new FileOutputStream(filePath)) {
                fos.write(encryptedData);
            }
        } catch (Exception e) {
            logger.error("CANIS database save failed");
            throw new IllegalStateException("Unable to save CANIS database");
        }
    }

    /** Creates an entry only when the key is absent, without replacing existing key material. */
    public boolean create(String key) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        StoredKey value = new StoredKey(
                key,
                Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()),
                Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
        lock.writeLock().lock();
        try {
            if (store.containsKey(key)) {
                return false;
            }
            store.put(key, value);
            try {
                saveToFile();
            } catch (RuntimeException e) {
                store.remove(key);
                throw e;
            }
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Entry get(String key) {
        lock.readLock().lock();
        try {
            StoredKey value = store.get(key);
            return value == null ? null : value.metadata();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void delete(String key) {
        lock.writeLock().lock();
        try {
            StoredKey removed = store.remove(key);
            try {
                saveToFile();
            } catch (RuntimeException e) {
                if (removed != null) {
                    store.put(key, removed);
                }
                throw e;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Entry> list() throws IOException {
        lock.readLock().lock();
        try {
            List<Entry> result = new ArrayList<>();
            for (StoredKey value : store.values()) {
                result.add(value.metadata());
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    /** Decrypts with a stored key without returning its private material. */
    public byte[] decrypt(String key, byte[] encryptedData) throws GeneralSecurityException {
        if (encryptedData == null || encryptedData.length == 0) {
            throw new GeneralSecurityException("Invalid encrypted input");
        }
        lock.readLock().lock();
        try {
            StoredKey value = store.get(key);
            if (value == null) {
                throw new NoSuchElementException("Key not found");
            }
            try {
                byte[] encoded = Base64.getDecoder().decode(value.privateKey);
                try {
                    PrivateKey privateKey =
                            KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encoded));
                    return Cryptographer.decrypt(encryptedData, privateKey);
                } finally {
                    Arrays.fill(encoded, (byte) 0);
                }
            } catch (GeneralSecurityException | IllegalArgumentException e) {
                throw new GeneralSecurityException("Unable to decrypt with stored key");
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private byte[] serializeStore(Map<String, StoredKey> map) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(byteStream)) {
            out.write(STORE_MAGIC);
            out.writeInt(map.size());
            for (Map.Entry<String, StoredKey> mapEntry : map.entrySet()) {
                StoredKey entry = mapEntry.getValue();
                writeString(out, mapEntry.getKey());
                writeString(out, entry.name);
                writeString(out, entry.publicKey);
                writeString(out, entry.privateKey);
            }
        }
        return byteStream.toByteArray();
    }

    private Map<String, StoredKey> deserializeVersionedStore(byte[] data) throws IOException {
        Map<String, StoredKey> result = new ConcurrentHashMap<>();
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
                if (key == null || name == null || publicKey == null || privateKey == null) {
                    throw new IOException("Invalid CANIS store entry.");
                }
                result.put(key, new StoredKey(name, publicKey, privateKey));
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

    private static final class StoredKey {
        private final String name;
        private final String publicKey;
        private final String privateKey;

        private StoredKey(String name, String publicKey, String privateKey) {
            this.name = name;
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        private Entry metadata() {
            return new Entry(name, publicKey);
        }
    }
}
