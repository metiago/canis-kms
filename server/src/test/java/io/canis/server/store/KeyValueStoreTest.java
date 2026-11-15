package io.canis.server.store;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class KeyValueStoreTest {

    private static final byte[] STORE_MAGIC = "CANISDB1".getBytes(StandardCharsets.US_ASCII);

    @TempDir
    Path tempDir;

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
    void malformedStoredPrivateKeyNeverAppearsInException() throws Exception {
        Path database = tempDir.resolve("malformed.db");
        Path secret = tempDir.resolve("malformed.key");
        String marker = "PRIVATE_MATERIAL_SENTINEL";
        Files.write(
                database,
                new SymmetricGenerator(secret.toString()).encrypt(legacyRecord("serviceA", "public", marker)));
        KeyValueStore store = new KeyValueStore(database.toString(), secret.toString());
        GeneralSecurityException failure =
                assertThrows(GeneralSecurityException.class, () -> store.decrypt("serviceA", new byte[] {1, 2, 3}));
        assertFalse(failure.toString().contains(marker));
        assertNull(failure.getCause());
    }

    @Test
    void saveFailureDoesNotAcknowledgeOrRetainKey() {
        Path database = tempDir.resolve("missing-parent").resolve("db.dat");
        Path secret = tempDir.resolve("save-failure.key");
        KeyValueStore store = new KeyValueStore(database.toString(), secret.toString());
        assertThrows(IllegalStateException.class, () -> store.create("serviceA"));
        assertNull(store.get("serviceA"));
        assertTrue(Files.notExists(database));
    }

    @Test
    void missingOrWrongMasterKeyAndTamperingFailClosed() throws Exception {
        Path database = tempDir.resolve("protected.db");
        Path secret = tempDir.resolve("protected.key");
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
        KeyValueStore store = new KeyValueStore(database.toString(), originalSecret.toString());
        store.create("serviceA");
        new SymmetricGenerator(wrongSecret.toString());
        byte[] original = Files.readAllBytes(database);

        assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), wrongSecret.toString()));
        assertArrayEquals(original, Files.readAllBytes(database));
    }

    @Test
    void invalidDecryptedStoreMarkerFailsClosed() throws Exception {
        Path database = tempDir.resolve("invalid-marker.db");
        Path secret = tempDir.resolve("invalid-marker.key");
        byte[] invalid = new SymmetricGenerator(secret.toString())
                .encrypt("invalid store marker".getBytes(StandardCharsets.UTF_8));
        Files.write(database, invalid);

        assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), secret.toString()));
        assertArrayEquals(invalid, Files.readAllBytes(database));
    }

    private KeyValueStore newStore(String name) {
        return new KeyValueStore(
                tempDir.resolve(name + ".db").toString(),
                tempDir.resolve(name + ".key").toString());
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
