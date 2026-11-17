package io.canis.store;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.canis.MasterKeyTestSupport;
import java.net.URI;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
    void interruptedReplacementRequiresRecoveryWithoutOverwritingBackup() throws Exception {
        Path database = tempDir.resolve("canis.db");
        Path secret = tempDir.resolve("master.key");
        MasterKeyTestSupport.provision(secret);
        KeyValueStore.initialize(database.toString(), secret.toString());
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
}
