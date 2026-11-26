package io.canis.store;

import io.canis.crypto.Cryptographer;
import io.canis.crypto.SymmetricGenerator;
import io.canis.protocol.ServiceNameValidator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KeyValueStore {

    private static final Logger logger = LoggerFactory.getLogger(KeyValueStore.class);
    private static final byte[] STORE_MAGIC = "CANISDB1".getBytes(StandardCharsets.US_ASCII);
    private static final int MAX_ENTRIES = 100_000;
    private static final int MAX_FIELD_BYTES = 1_000_000;
    private static final int MAX_DATABASE_BYTES = 256 * 1024 * 1024;
    private Map<String, StoredKey> store;
    private final Path filePath;
    private final Path recoveryPath;
    private final Path keyPath;
    private final SymmetricGenerator symmetricGenerator;
    private final Consumer<ReplacementPoint> replacementPoint;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private byte[] expectedDigest;
    private Object expectedFileKey;
    private volatile boolean healthy = true;

    public KeyValueStore() {
        this("db.dat", System.getenv("CANIS_SECRET_KEY"));
    }

    public KeyValueStore(String filePath, String secretKeyPath) {
        this(filePath, secretKeyPath, ignored -> {});
    }

    KeyValueStore(String filePath, String secretKeyPath, Consumer<ReplacementPoint> replacementPoint) {
        this.symmetricGenerator = new SymmetricGenerator(secretKeyPath);
        this.replacementPoint = replacementPoint;
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalStateException("CANIS database path is not configured");
        }
        try {
            this.filePath = Path.of(filePath).toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            throw new IllegalStateException("CANIS database path is invalid");
        }
        this.recoveryPath = this.filePath.resolveSibling(this.filePath.getFileName() + ".canis-recovery");
        this.keyPath = Path.of(secretKeyPath).toAbsolutePath().normalize();
        this.store = new ConcurrentHashMap<>();
        loadFromFile();
    }

    /** Explicit, one-time initialization. Refuses to replace any existing database. */
    public static void initialize(String filePath, String secretKeyPath) {
        SymmetricGenerator cipher = new SymmetricGenerator(secretKeyPath);
        Path database;
        try {
            database = Path.of(filePath).toAbsolutePath().normalize();
        } catch (InvalidPathException | NullPointerException e) {
            throw new IllegalStateException("CANIS database path is invalid");
        }
        try {
            requireNoRecoveryFile(database.resolveSibling(database.getFileName() + ".canis-recovery"));
            byte[] plain = new byte[STORE_MAGIC.length + Integer.BYTES];
            System.arraycopy(STORE_MAGIC, 0, plain, 0, STORE_MAGIC.length);
            byte[] encrypted = cipher.encrypt(plain);
            try (FileChannel output = FileChannel.open(
                    database, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS)) {
                java.nio.ByteBuffer bytes = java.nio.ByteBuffer.wrap(encrypted);
                while (bytes.hasRemaining()) {
                    output.write(bytes);
                }
                output.force(true);
            }
            logger.info("CANIS database explicitly initialized");
        } catch (Exception e) {
            logger.error("CANIS database initialization rejected");
            throw new IllegalStateException("Unable to initialize CANIS database");
        }
    }

    /** Confirms that an injected store uses the configured key path and remains ready. */
    public void verifyConfiguration(Path configuredKeyPath) {
        if (configuredKeyPath == null
                || !keyPath.equals(configuredKeyPath.toAbsolutePath().normalize())
                || !symmetricGenerator.matchesKeyFile(configuredKeyPath.toString())) {
            throw new IllegalStateException("CANIS database key configuration mismatch");
        }
        ensureHealthy();
    }

    public boolean isHealthy() {
        return healthy;
    }

    private void loadFromFile() {
        lock.writeLock().lock();
        try {
            try {
                byte[] encryptedData = readDatabase();
                byte[] decryptedData = symmetricGenerator.decrypt(encryptedData);
                try {
                    store = deserializeVersionedStore(decryptedData);
                } finally {
                    Arrays.fill(decryptedData, (byte) 0);
                }
                remember(encryptedData);
            } catch (Exception e) {
                logger.error("CANIS database startup rejected: missing, unreadable, or invalid database");
                throw new IllegalStateException("Unable to load CANIS database; recovery required");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void saveToFile() {
        Path temporary = null;
        boolean oldDatabaseMoved = false;
        boolean recoveryReserved = false;
        try {
            ensureHealthy();
            byte[] serializedData = serializeStore(store);
            byte[] encryptedData = symmetricGenerator.encrypt(serializedData);
            temporary = Files.createTempFile(filePath.getParent(), ".canis-db-", ".tmp");
            try (FileChannel output =
                    FileChannel.open(temporary, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS)) {
                java.nio.ByteBuffer bytes = java.nio.ByteBuffer.wrap(encryptedData);
                while (bytes.hasRemaining()) {
                    output.write(bytes);
                }
                output.force(true);
            }
            ensureHealthy();
            Files.createFile(recoveryPath);
            recoveryReserved = true;
            moveExistingDatabase(filePath, recoveryPath);
            oldDatabaseMoved = true;
            replacementPoint.accept(ReplacementPoint.OLD_MOVED);
            Files.move(temporary, filePath, StandardCopyOption.ATOMIC_MOVE);
            replacementPoint.accept(ReplacementPoint.NEW_INSTALLED);
            remember(encryptedData);
            Files.delete(recoveryPath);
        } catch (Exception e) {
            if (recoveryReserved && !oldDatabaseMoved) {
                try {
                    Files.deleteIfExists(recoveryPath);
                } catch (IOException cleanupFailure) {
                    logger.warn("CANIS database recovery reservation cleanup failed");
                }
            }
            fail("save_failed");
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException e) {
                    logger.warn("CANIS database temporary file cleanup failed");
                }
            }
        }
    }

    static void moveExistingDatabase(Path database, Path recovery) throws IOException {
        Files.move(database, recovery, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    enum ReplacementPoint {
        OLD_MOVED,
        NEW_INSTALLED
    }

    public void ensureHealthy() {
        lock.readLock().lock();
        try {
            if (!healthy) {
                throw new IllegalStateException("CANIS database unavailable; recovery required");
            }
            byte[] current = readDatabase();
            if (!MessageDigest.isEqual(expectedDigest, digest(current))
                    || (expectedFileKey != null && !expectedFileKey.equals(fileKey()))) {
                fail("changed_database");
            }
        } catch (IOException | SecurityException e) {
            fail("missing_or_unreadable_database");
        } finally {
            lock.readLock().unlock();
        }
    }

    private byte[] readDatabase() throws IOException {
        requireNoRecoveryFile(recoveryPath);
        BasicFileAttributes before =
                Files.readAttributes(filePath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        if (!before.isRegularFile() || before.size() == 0 || before.size() > MAX_DATABASE_BYTES) {
            throw new IOException("Invalid database file type");
        }
        byte[] data;
        try (var input = Files.newInputStream(filePath, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
            data = input.readNBytes(MAX_DATABASE_BYTES + 1);
        }
        BasicFileAttributes after =
                Files.readAttributes(filePath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        if (!after.isRegularFile()
                || data.length == 0
                || data.length > MAX_DATABASE_BYTES
                || after.size() != data.length
                || (before.fileKey() != null && !before.fileKey().equals(after.fileKey()))) {
            throw new IOException("Database changed while reading");
        }
        return data;
    }

    private static void requireNoRecoveryFile(Path recovery) throws IOException {
        try {
            Files.readAttributes(recovery, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            throw new IOException("Database recovery required");
        } catch (NoSuchFileException expected) {
            // A confirmed absent recovery file is the only state that permits normal operation.
        }
    }

    private Object fileKey() throws IOException {
        return Files.readAttributes(filePath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS)
                .fileKey();
    }

    private void remember(byte[] encrypted) throws IOException {
        expectedDigest = digest(encrypted);
        expectedFileKey = fileKey();
    }

    private static byte[] digest(byte[] value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Required database digest unavailable");
        }
    }

    private void fail(String category) {
        healthy = false;
        logger.error("CANIS database entered recovery-required state: {}", category);
        throw new IllegalStateException("CANIS database unavailable; recovery required");
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
            ensureHealthy();
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
            ensureHealthy();
            StoredKey value = store.get(key);
            return value == null ? null : value.metadata();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void delete(String key) {
        lock.writeLock().lock();
        try {
            ensureHealthy();
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
            ensureHealthy();
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
            ensureHealthy();
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
                if (key == null
                        || name == null
                        || publicKey == null
                        || privateKey == null
                        || !key.equals(name)
                        || !ServiceNameValidator.isValid(name)) {
                    throw new IOException("Invalid CANIS store entry.");
                }
                validateKeyPair(publicKey, privateKey);
                if (result.putIfAbsent(key, new StoredKey(name, publicKey, privateKey)) != null) {
                    throw new IOException("Duplicate CANIS store entry");
                }
            }
            if (in.read() != -1) {
                throw new IOException("Trailing CANIS store data");
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
        if (length < 0 || length > MAX_FIELD_BYTES || length > in.available()) {
            return null;
        }
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(java.nio.ByteBuffer.wrap(bytes))
                .toString();
    }

    private static void validateKeyPair(String publicEncoding, String privateEncoding) throws IOException {
        byte[] publicBytes = null;
        byte[] privateBytes = null;
        try {
            publicBytes = Base64.getDecoder().decode(publicEncoding);
            privateBytes = Base64.getDecoder().decode(privateEncoding);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(publicBytes));
            PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
            if (!(publicKey instanceof RSAPublicKey rsaPublic)
                    || !(privateKey instanceof RSAPrivateKey rsaPrivate)
                    || !rsaPublic.getModulus().equals(rsaPrivate.getModulus())
                    || !Arrays.equals(publicBytes, publicKey.getEncoded())
                    || !Arrays.equals(privateBytes, privateKey.getEncoded())) {
                throw new IOException("Invalid CANIS store RSA key pair");
            }
            Signature proof = Signature.getInstance("SHA256withRSA");
            proof.initSign(privateKey);
            proof.update(STORE_MAGIC);
            byte[] signature = proof.sign();
            proof.initVerify(publicKey);
            proof.update(STORE_MAGIC);
            if (!proof.verify(signature)) {
                throw new IOException("Invalid CANIS store RSA key pair");
            }
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IOException("Invalid CANIS store RSA key pair");
        } finally {
            if (publicBytes != null) {
                Arrays.fill(publicBytes, (byte) 0);
            }
            if (privateBytes != null) {
                Arrays.fill(privateBytes, (byte) 0);
            }
        }
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
