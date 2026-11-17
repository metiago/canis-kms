package io.canis.server.store;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.canis.MasterKeyTestSupport;
import io.canis.crypto.Cryptographer;
import io.canis.crypto.SymmetricGenerator;
import io.canis.store.Entry;
import io.canis.store.KeyValueStore;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

class KeyValueStoreTest {

    private static final byte[] STORE_MAGIC = "CANISDB1".getBytes(StandardCharsets.US_ASCII);

    @TempDir
    Path tempDir;

    @Test
    void databaseRequiresExplicitInitializationAndNeverOverwritesExistingFile() throws Exception {
        Path database = tempDir.resolve("new.db");
        Path secret = tempDir.resolve("new.key");
        assertThrows(
                IllegalStateException.class, () -> KeyValueStore.initialize(database.toString(), secret.toString()));
        assertTrue(Files.notExists(database));
        MasterKeyTestSupport.provision(secret);
        assertThrows(IllegalStateException.class, () -> KeyValueStore.initialize(null, secret.toString()));

        IllegalStateException failure = assertThrows(
                IllegalStateException.class, () -> new KeyValueStore(database.toString(), secret.toString()));
        assertFalse(failure.toString().contains(database.toString()));
        assertFalse(failure.toString().contains(secret.toString()));
        assertTrue(Files.notExists(database));

        KeyValueStore.initialize(database.toString(), secret.toString());
        byte[] initialized = Files.readAllBytes(database);
        assertTrue(
                new KeyValueStore(database.toString(), secret.toString()).list().isEmpty());
        assertThrows(
                IllegalStateException.class, () -> KeyValueStore.initialize(database.toString(), secret.toString()));
        assertArrayEquals(initialized, Files.readAllBytes(database));
    }

    @Test
    void runningStoreRejectsDatabaseLossAndTamperingWithoutRecreation() throws Exception {
        Path database = tempDir.resolve("runtime.db");
        Path secret = tempDir.resolve("runtime.key");
        MasterKeyTestSupport.provision(secret);
        KeyValueStore.initialize(database.toString(), secret.toString());
        KeyValueStore store = new KeyValueStore(database.toString(), secret.toString());
        Files.delete(database);

        assertThrows(IllegalStateException.class, () -> store.get("serviceA"));
        assertThrows(IllegalStateException.class, () -> store.create("serviceA"));
        assertFalse(store.isHealthy());
        assertTrue(Files.notExists(database));

        KeyValueStore.initialize(database.toString(), secret.toString());
        KeyValueStore second = new KeyValueStore(database.toString(), secret.toString());
        byte[] tampered = Files.readAllBytes(database);
        tampered[tampered.length - 1] ^= 1;
        Files.write(database, tampered);
        assertThrows(IllegalStateException.class, second::list);
        assertFalse(second.isHealthy());
        assertArrayEquals(tampered, Files.readAllBytes(database));
    }

    @Test
    void authenticatedButMalformedRecordsAreRejected() throws Exception {
        Path database = tempDir.resolve("malformed-records.db");
        Path secret = tempDir.resolve("malformed-records.key");
        MasterKeyTestSupport.provision(secret);
        byte[] valid = legacyRecord("serviceA", "public", "private");
        byte[] trailing = Arrays.copyOf(valid, valid.length + 1);
        Files.write(database, new SymmetricGenerator(secret.toString()).encrypt(trailing));
        assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), secret.toString()));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.write(STORE_MAGIC);
            out.writeInt(1);
            out.writeInt(Integer.MAX_VALUE);
        }
        byte[] oversized = new SymmetricGenerator(secret.toString()).encrypt(bytes.toByteArray());
        Files.write(database, oversized);
        assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), secret.toString()));
        assertArrayEquals(oversized, Files.readAllBytes(database));

        bytes.reset();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.write(STORE_MAGIC);
            out.writeInt(2);
            for (int i = 0; i < 2; i++) {
                writeString(out, "serviceA");
                writeString(out, "serviceA");
                writeString(out, "public");
                writeString(out, "private");
            }
        }
        byte[] duplicated = new SymmetricGenerator(secret.toString()).encrypt(bytes.toByteArray());
        Files.write(database, duplicated);
        assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), secret.toString()));
        assertArrayEquals(duplicated, Files.readAllBytes(database));
    }

    @Test
    void createReturnsOnlyPublicMetadataAndDecrypts() throws Exception {
        KeyValueStore store = newStore("active");
        assertTrue(store.create("serviceA"));
        Entry entry = store.get("serviceA");
        assertNotNull(entry);
        assertEquals("serviceA", entry.name());
        assertEquals(1, store.list().size());
        assertEquals(entry, store.list().get(0));
        assertFalse(store.create("serviceA"));
        assertEquals(entry, store.get("serviceA"));

        var publicKey = java.security.KeyFactory.getInstance("RSA")
                .generatePublic(new java.security.spec.X509EncodedKeySpec(
                        Base64.getDecoder().decode(entry.publicKey())));
        byte[] plaintext = "file key".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(plaintext, store.decrypt("serviceA", Cryptographer.encrypt(plaintext, publicKey)));
        assertThrows(GeneralSecurityException.class, () -> store.decrypt("serviceA", new byte[] {1, 2, 3}));
        assertThrows(GeneralSecurityException.class, () -> store.decrypt("serviceA", new byte[0]));
        assertThrows(NoSuchElementException.class, () -> store.decrypt("missing", new byte[] {1}));

        store.delete("serviceA");
        assertNull(store.get("serviceA"));
    }

    @Test
    void metadataCannotBeSerializedAsAKeyBearingObject() throws Exception {
        Entry entry = new Entry("serviceA", "public-key");
        assertThrows(NotSerializableException.class, () -> {
            try (ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream())) {
                out.writeObject(entry);
            }
        });
        assertEquals(2, Entry.class.getRecordComponents().length);
        assertFalse(Arrays.stream(Entry.class.getMethods())
                .anyMatch(method -> method.getName().toLowerCase().contains("private")));
        assertFalse(Arrays.stream(KeyValueStore.class.getMethods())
                .anyMatch(method -> PrivateKey.class.isAssignableFrom(method.getReturnType())
                        || KeyPair.class.isAssignableFrom(method.getReturnType())));
    }

    @Test
    void legacyEncryptedDatabaseStillLoadsAndDecrypts() throws Exception {
        Path database = tempDir.resolve("legacy.db");
        Path secret = tempDir.resolve("legacy.key");
        MasterKeyTestSupport.provision(secret);
        KeyPair pair = generatePair();
        String privateKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
        byte[] legacy = legacyRecord(
                "serviceA", Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()), privateKey);
        Files.write(database, new SymmetricGenerator(secret.toString()).encrypt(legacy));

        KeyValueStore store = new KeyValueStore(database.toString(), secret.toString());
        Entry metadata = store.get("serviceA");
        assertNotNull(metadata);
        assertFalse(metadata.toString().contains(privateKey));
        assertFalse(store.list().toString().contains(privateKey));
        byte[] plaintext = "legacy file key".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(plaintext, store.decrypt("serviceA", Cryptographer.encrypt(plaintext, pair.getPublic())));

        assertTrue(store.create("serviceB"));
        byte[] saved = new SymmetricGenerator(secret.toString()).decrypt(Files.readAllBytes(database));
        assertArrayEquals(STORE_MAGIC, Arrays.copyOf(saved, STORE_MAGIC.length));
        KeyValueStore reloaded = new KeyValueStore(database.toString(), secret.toString());
        assertArrayEquals(plaintext, reloaded.decrypt("serviceA", Cryptographer.encrypt(plaintext, pair.getPublic())));
    }

    @Test
    void malformedStoredPrivateKeyIsRejectedBeforeServingAndNeverAppearsInException() throws Exception {
        Path database = tempDir.resolve("malformed.db");
        Path secret = tempDir.resolve("malformed.key");
        MasterKeyTestSupport.provision(secret);
        String marker = "PRIVATE_MATERIAL_SENTINEL";
        Files.write(
                database,
                new SymmetricGenerator(secret.toString()).encrypt(legacyRecord("serviceA", "public", marker)));
        byte[] original = Files.readAllBytes(database);
        Logger logger = (Logger) LoggerFactory.getLogger(KeyValueStore.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        IllegalStateException failure;
        try {
            failure = assertThrows(
                    IllegalStateException.class, () -> new KeyValueStore(database.toString(), secret.toString()));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
        assertFalse(failure.toString().contains(marker));
        assertNull(failure.getCause());
        assertFalse(appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains(marker)));
        assertArrayEquals(original, Files.readAllBytes(database));
    }

    @Test
    void privateEncodingInPublicFieldAndMismatchedPairFailBeforeMetadataExposure() throws Exception {
        Path database = tempDir.resolve("invalid-pair.db");
        Path secret = tempDir.resolve("invalid-pair.key");
        MasterKeyTestSupport.provision(secret);
        KeyPair first = generatePair();
        KeyPair second = generatePair();
        String publicKey = Base64.getEncoder().encodeToString(first.getPublic().getEncoded());
        String privateKey =
                Base64.getEncoder().encodeToString(first.getPrivate().getEncoded());
        String otherPrivateKey =
                Base64.getEncoder().encodeToString(second.getPrivate().getEncoded());
        for (byte[] record : List.of(
                legacyRecord("serviceA", privateKey, privateKey),
                legacyRecord("serviceA", publicKey, otherPrivateKey),
                legacyRecord("serviceA", publicKey, "not-base64"))) {
            byte[] encrypted = new SymmetricGenerator(secret.toString()).encrypt(record);
            Files.write(database, encrypted);
            IllegalStateException failure = assertThrows(
                    IllegalStateException.class, () -> new KeyValueStore(database.toString(), secret.toString()));
            assertFalse(failure.toString().contains(privateKey));
            assertFalse(failure.toString().contains(otherPrivateKey));
            assertArrayEquals(encrypted, Files.readAllBytes(database));
        }
    }

    @Test
    void malformedUtf8AndMismatchedRecordNameFailWithoutRewrite() throws Exception {
        Path database = tempDir.resolve("invalid-legacy.db");
        Path secret = tempDir.resolve("invalid-legacy.key");
        MasterKeyTestSupport.provision(secret);
        KeyPair pair = generatePair();
        String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.write(STORE_MAGIC);
            out.writeInt(1);
            out.writeInt(1);
            out.writeByte(0xff);
            writeString(out, "serviceA");
            writeString(out, publicKey);
            writeString(out, privateKey);
        }
        for (byte[] record : List.of(bytes.toByteArray(), mismatchedNameRecord(publicKey, privateKey))) {
            byte[] encrypted = new SymmetricGenerator(secret.toString()).encrypt(record);
            Files.write(database, encrypted);
            assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), secret.toString()));
            assertArrayEquals(encrypted, Files.readAllBytes(database));
        }
    }

    private byte[] mismatchedNameRecord(String publicKey, String privateKey) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.write(STORE_MAGIC);
            out.writeInt(1);
            writeString(out, "serviceA");
            writeString(out, "serviceB");
            writeString(out, publicKey);
            writeString(out, privateKey);
        }
        return bytes.toByteArray();
    }

    @Test
    void saveFailureDoesNotAcknowledgeOrRetainKey() throws Exception {
        Path database = tempDir.resolve("PRIVATE_MATERIAL_SENTINEL-save-failure.db");
        Path secret = tempDir.resolve("PRIVATE_MATERIAL_SENTINEL-save-failure.key");
        MasterKeyTestSupport.provision(secret);
        KeyValueStore.initialize(database.toString(), secret.toString());
        KeyValueStore store = new KeyValueStore(database.toString(), secret.toString());
        Files.delete(database);
        Logger logger = (Logger) LoggerFactory.getLogger(KeyValueStore.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            assertThrows(IllegalStateException.class, () -> store.create("serviceA"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
        assertTrue(appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains("recovery-required")));
        assertFalse(appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains("PRIVATE_MATERIAL_SENTINEL")));
        assertFalse(store.isHealthy());
        assertTrue(Files.notExists(database));
    }

    @Test
    void symlinkedDatabaseIsRejectedWithoutChangingItsTarget() throws Exception {
        Path database = tempDir.resolve("real.db");
        Path link = tempDir.resolve("linked.db");
        Path secret = tempDir.resolve("master.key");
        MasterKeyTestSupport.provision(secret);
        KeyValueStore.initialize(database.toString(), secret.toString());
        byte[] original = Files.readAllBytes(database);
        try {
            Files.createSymbolicLink(link, database);
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            assumeTrue(false, "Symbolic links cannot be created on this platform");
        }

        assertThrows(IllegalStateException.class, () -> new KeyValueStore(link.toString(), secret.toString()));
        assertArrayEquals(original, Files.readAllBytes(database));
    }

    @Test
    void missingOrWrongMasterKeyAndTamperingFailClosed() throws Exception {
        Path database = tempDir.resolve("protected.db");
        Path secret = tempDir.resolve("protected.key");
        MasterKeyTestSupport.provision(secret);
        KeyValueStore.initialize(database.toString(), secret.toString());
        KeyValueStore store = new KeyValueStore(database.toString(), secret.toString());
        store.create("serviceA");
        byte[] original = Files.readAllBytes(database);

        Path wrong = tempDir.resolve("wrong.key");
        assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), wrong.toString()));
        assertTrue(Files.notExists(wrong));

        byte[] tampered = original.clone();
        tampered[tampered.length - 1] ^= 1;
        Files.write(database, tampered);
        assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), secret.toString()));
        assertArrayEquals(tampered, Files.readAllBytes(database));

        Files.write(database, original);
        Files.delete(secret);
        assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), secret.toString()));
        assertArrayEquals(original, Files.readAllBytes(database));
        assertTrue(Files.notExists(secret));
    }

    @Test
    void wrongExistingMasterKeyDoesNotReplaceDatabase() throws Exception {
        Path database = tempDir.resolve("wrong-key.db");
        Path originalSecret = tempDir.resolve("original.key");
        Path wrongSecret = tempDir.resolve("different.key");
        MasterKeyTestSupport.provision(originalSecret);
        MasterKeyTestSupport.provision(wrongSecret);
        KeyValueStore.initialize(database.toString(), originalSecret.toString());
        KeyValueStore store = new KeyValueStore(database.toString(), originalSecret.toString());
        store.create("serviceA");
        byte[] original = Files.readAllBytes(database);

        assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), wrongSecret.toString()));
        assertArrayEquals(original, Files.readAllBytes(database));
    }

    @Test
    void runningStoreUsesLoadedKeyWithoutRecreatingDeletedKeyFile() throws Exception {
        Path database = tempDir.resolve("loaded-once.db");
        Path secret = tempDir.resolve("loaded-once.key");
        MasterKeyTestSupport.provision(secret);
        KeyValueStore.initialize(database.toString(), secret.toString());
        KeyValueStore store = new KeyValueStore(database.toString(), secret.toString());

        Files.delete(secret);
        assertTrue(store.create("serviceA"));
        assertFalse(Files.exists(secret));
        byte[] saved = Files.readAllBytes(database);
        assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), secret.toString()));
        assertArrayEquals(saved, Files.readAllBytes(database));
    }

    @Test
    void invalidDecryptedStoreMarkerFailsClosed() throws Exception {
        Path database = tempDir.resolve("invalid-marker.db");
        Path secret = tempDir.resolve("invalid-marker.key");
        MasterKeyTestSupport.provision(secret);
        byte[] invalid = new SymmetricGenerator(secret.toString())
                .encrypt("invalid store marker".getBytes(StandardCharsets.UTF_8));
        Files.write(database, invalid);

        assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), secret.toString()));
        assertArrayEquals(invalid, Files.readAllBytes(database));
    }

    private KeyValueStore newStore(String name) throws Exception {
        Path secret = tempDir.resolve(name + ".key");
        MasterKeyTestSupport.provision(secret);
        Path database = tempDir.resolve(name + ".db");
        KeyValueStore.initialize(database.toString(), secret.toString());
        return new KeyValueStore(database.toString(), secret.toString());
    }

    private KeyPair generatePair() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private byte[] legacyRecord(String name, String publicKey, String privateKey) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.write(STORE_MAGIC);
            out.writeInt(1);
            writeString(out, name);
            writeString(out, name);
            writeString(out, publicKey);
            writeString(out, privateKey);
        }
        return bytes.toByteArray();
    }

    private void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }
}
