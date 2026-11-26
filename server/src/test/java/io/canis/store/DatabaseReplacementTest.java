package io.canis.store;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.canis.MasterKeyTestSupport;
import io.canis.crypto.Cryptographer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

class DatabaseReplacementTest {
    @TempDir
    Path tempDir;

    @Test
    void deletionAfterVerificationCannotBecomeAReplacementWrite() throws Exception {
        Path database = tempDir.resolve("canis.db");
        Path recovery = tempDir.resolve("canis.db.canis-recovery");
        Path staged = tempDir.resolve("staged.db");
        Files.write(database, new byte[] {1, 2, 3});
        Files.write(staged, new byte[] {4, 5, 6});
        assertArrayEquals(new byte[] {1, 2, 3}, Files.readAllBytes(database));
        Files.createFile(recovery);
        Files.delete(database);

        assertThrows(NoSuchFileException.class, () -> KeyValueStore.moveExistingDatabase(database, recovery));
        assertFalse(Files.exists(database));
        assertArrayEquals(new byte[] {4, 5, 6}, Files.readAllBytes(staged));
    }

    @Test
    void interruptedReplacementRestoresOriginalDatabaseAndDecryptsExistingEnvelope() throws Exception {
        Path database = tempDir.resolve("canis.db");
        Path secret = tempDir.resolve("master.key");
        MasterKeyTestSupport.provision(secret);
        KeyValueStore.initialize(database.toString(), secret.toString());
        KeyValueStore store = new KeyValueStore(database.toString(), secret.toString());
        assertTrue(store.create("serviceA"));
        byte[] publicKeyBytes = Base64.getDecoder().decode(store.get("serviceA").publicKey());
        var publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        byte[] plaintext = "key encrypted before interrupted replacement".getBytes(StandardCharsets.UTF_8);
        byte[] envelope = Cryptographer.encrypt(plaintext, publicKey);
        byte[] original = Files.readAllBytes(database);
        Path recovery = tempDir.resolve("canis.db.canis-recovery");
        Files.createFile(recovery);
        KeyValueStore.moveExistingDatabase(database, recovery);

        assertFalse(Files.exists(database));
        assertArrayEquals(original, Files.readAllBytes(recovery));
        assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), secret.toString()));
        assertThrows(
                IllegalStateException.class, () -> KeyValueStore.initialize(database.toString(), secret.toString()));
        assertTrue(Files.exists(recovery));
        assertFalse(Files.exists(database));

        Files.move(recovery, database);
        assertArrayEquals(original, Files.readAllBytes(database));
        assertFalse(Files.exists(recovery));
        KeyValueStore restored = new KeyValueStore(database.toString(), secret.toString());
        assertArrayEquals(plaintext, restored.decrypt("serviceA", envelope));

        Files.createFile(recovery);
        assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), secret.toString()));
        assertThrows(
                IllegalStateException.class, () -> KeyValueStore.initialize(database.toString(), secret.toString()));
        assertArrayEquals(original, Files.readAllBytes(database));
    }

    @Test
    void unsupportedAtomicMoveLeavesOriginalDatabaseInPlace() throws Exception {
        Path database = tempDir.resolve("canis.db");
        byte[] original = new byte[] {1, 2, 3};
        Files.write(database, original);
        Path archive = tempDir.resolve("separate-provider.zip");
        try (var zip = FileSystems.newFileSystem(URI.create("jar:" + archive.toUri()), Map.of("create", "true"))) {
            Path recovery = zip.getPath("/recovery.db");
            assertThrows(
                    AtomicMoveNotSupportedException.class,
                    () -> KeyValueStore.moveExistingDatabase(database, recovery));
            assertArrayEquals(original, Files.readAllBytes(database));
            assertFalse(Files.exists(recovery));
        }
    }

    @Test
    void posixSymlinkedDatabaseFailsClosedWithSanitizedLog() throws Exception {
        assumeTrue(Files.getFileStore(tempDir).supportsFileAttributeView("posix"));
        Path database = tempDir.resolve("PRIVATE_MATERIAL_SENTINEL.db");
        Path target = tempDir.resolve("target.db");
        Path secret = tempDir.resolve("master.key");
        MasterKeyTestSupport.provision(secret);
        KeyValueStore.initialize(database.toString(), secret.toString());
        KeyValueStore store = new KeyValueStore(database.toString(), secret.toString());
        byte[] original = Files.readAllBytes(database);
        Files.move(database, target);
        Files.createSymbolicLink(database, target);

        ListAppender<ILoggingEvent> logs = captureLogs();
        try {
            assertThrows(IllegalStateException.class, () -> store.create("serviceA"));
        } finally {
            stopCapturing(logs);
        }
        assertFalse(store.isHealthy());
        assertTrue(Files.isSymbolicLink(database));
        assertArrayEquals(original, Files.readAllBytes(target));
        assertFalse(Files.exists(tempDir.resolve("PRIVATE_MATERIAL_SENTINEL.db.canis-recovery")));
        assertSanitized(logs);
    }

    @Test
    void posixUnwritableDirectoryFailsClosedWithSanitizedLog() throws Exception {
        assumeTrue(Files.getFileStore(tempDir).supportsFileAttributeView("posix"));
        Path directory = Files.createDirectory(tempDir.resolve("PRIVATE_MATERIAL_SENTINEL-dir"));
        Path database = directory.resolve("canis.db");
        Path secret = tempDir.resolve("master.key");
        MasterKeyTestSupport.provision(secret);
        KeyValueStore.initialize(database.toString(), secret.toString());
        KeyValueStore store = new KeyValueStore(database.toString(), secret.toString());
        byte[] original = Files.readAllBytes(database);
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(directory);
        ListAppender<ILoggingEvent> logs = captureLogs();
        try {
            Files.setPosixFilePermissions(
                    directory, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE));
            assertFalse(Files.isWritable(directory), "Test requires an unwritable database directory");
            assertThrows(IllegalStateException.class, () -> store.create("serviceA"));
        } finally {
            Files.setPosixFilePermissions(directory, permissions);
            stopCapturing(logs);
        }
        assertFalse(store.isHealthy());
        assertArrayEquals(original, Files.readAllBytes(database));
        assertFalse(Files.exists(directory.resolve("canis.db.canis-recovery")));
        assertSanitized(logs);
    }

    private static ListAppender<ILoggingEvent> captureLogs() {
        ListAppender<ILoggingEvent> logs = new ListAppender<>();
        logs.start();
        ((Logger) LoggerFactory.getLogger(KeyValueStore.class)).addAppender(logs);
        return logs;
    }

    private static void stopCapturing(ListAppender<ILoggingEvent> logs) {
        ((Logger) LoggerFactory.getLogger(KeyValueStore.class)).detachAppender(logs);
        logs.stop();
    }

    private static void assertSanitized(ListAppender<ILoggingEvent> logs) {
        assertTrue(logs.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains("recovery-required")));
        assertFalse(logs.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(message -> message.contains("PRIVATE_MATERIAL_SENTINEL")));
    }
}
