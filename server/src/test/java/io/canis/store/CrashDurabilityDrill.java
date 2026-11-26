package io.canis.store;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.canis.MasterKeyTestSupport;
import io.canis.crypto.Cryptographer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import org.junit.jupiter.api.Test;

/** Manual VM power-loss drill. See CRASH-DURABILITY.md in this package. */
class CrashDurabilityDrill {
    private static final byte[] KNOWN = "CANIS crash durability fixture".getBytes(StandardCharsets.UTF_8);

    @Test
    void crashDurabilityDrill() throws Exception {
        String action = System.getProperty("canis.crash.action");
        assumeTrue(action != null, "Run manually with canis.crash.action; see CRASH-DURABILITY.md");
        String directory = System.getProperty("canis.crash.dir");
        if (directory == null || directory.isBlank()) {
            throw new IllegalArgumentException("Set canis.crash.dir to a disposable directory");
        }
        Path root = Path.of(directory).toAbsolutePath().normalize();
        switch (action) {
            case "prepare" -> prepare(root);
            case "write" -> write(root, stage());
            case "verify" -> verify(root, stage());
            default -> throw new IllegalArgumentException("Unknown crash drill action: " + action);
        }
    }

    private static void prepare(Path root) throws Exception {
        Files.createDirectory(root);
        Path database = root.resolve("db.dat");
        Path key = root.resolve("master.key");
        MasterKeyTestSupport.provision(key);
        KeyValueStore.initialize(database.toString(), key.toString());
        KeyValueStore store = new KeyValueStore(database.toString(), key.toString());
        assertTrue(store.create("baseline"));
        byte[] publicBytes = Base64.getDecoder().decode(store.get("baseline").publicKey());
        var publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicBytes));
        Files.write(
                root.resolve("known.canis"), Cryptographer.encrypt(KNOWN, publicKey), StandardOpenOption.CREATE_NEW);
        System.out.println("CANIS_CRASH_PREPARED");
    }

    private static void write(Path root, Stage stage) throws Exception {
        Path database = root.resolve("db.dat");
        Path key = root.resolve("master.key");
        KeyValueStore store = new KeyValueStore(database.toString(), key.toString(), point -> {
            if (stage.name().equals(point.name())) {
                stopAt(stage);
            }
        });
        assertTrue(store.get("baseline") != null);
        assertTrue(store.create("next"));
        if (stage == Stage.RETURNED) {
            stopAt(stage);
        }
    }

    private static void verify(Path root, Stage stage) throws Exception {
        Path database = root.resolve("db.dat");
        Path recovery = root.resolve("db.dat.canis-recovery");
        Path key = root.resolve("master.key");
        byte[] envelope = Files.readAllBytes(root.resolve("known.canis"));
        boolean recoveryExists = Files.exists(recovery, LinkOption.NOFOLLOW_LINKS);
        if (recoveryExists) {
            assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), key.toString()));
            assertThrows(
                    IllegalStateException.class, () -> KeyValueStore.initialize(database.toString(), key.toString()));
        }
        Candidate active = inspect(root, database, key, envelope);
        Candidate old = inspect(root, recovery, key, envelope);
        assertTrue(active.valid() || old.valid(), "Neither database path can decrypt the original envelope");
        if (stage == Stage.RETURNED) {
            assertFalse(recoveryExists, "Acknowledged write left unresolved recovery state");
            assertTrue(active.valid() && active.hasNext(), "Acknowledged write did not survive the crash");
        }
        System.out.println(
                "CANIS_CRASH_VERIFIED stage=" + stage.label + " active=" + active.valid() + " recovery=" + old.valid());
    }

    private static Candidate inspect(Path root, Path source, Path key, byte[] envelope) throws Exception {
        if (!Files.exists(source, LinkOption.NOFOLLOW_LINKS)) {
            return new Candidate(false, false);
        }
        Path copy = Files.createTempFile(root, ".canis-check-", ".db");
        try {
            Files.copy(source, copy, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS);
            KeyValueStore store = new KeyValueStore(copy.toString(), key.toString());
            return new Candidate(Arrays.equals(KNOWN, store.decrypt("baseline", envelope)), store.get("next") != null);
        } catch (Exception invalidCandidate) {
            return new Candidate(false, false);
        } finally {
            Files.deleteIfExists(copy);
        }
    }

    private static void stopAt(Stage stage) {
        System.out.println("CANIS_CRASH_READY stage=" + stage.label);
        System.out.flush();
        String mode = System.getProperty("canis.crash.mode", "pause");
        if ("halt".equals(mode)) {
            Runtime.getRuntime().halt(77);
        }
        if (!"pause".equals(mode)) {
            throw new IllegalArgumentException("canis.crash.mode must be pause or halt");
        }
        while (true) {
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException ignored) {
                // Only the VM host should end the paused durability drill.
            }
        }
    }

    private static Stage stage() {
        return switch (System.getProperty("canis.crash.stage", "")) {
            case "old-moved" -> Stage.OLD_MOVED;
            case "new-installed" -> Stage.NEW_INSTALLED;
            case "returned" -> Stage.RETURNED;
            default ->
                throw new IllegalArgumentException("Set canis.crash.stage to old-moved, new-installed, or returned");
        };
    }

    private record Candidate(boolean valid, boolean hasNext) {}

    private enum Stage {
        OLD_MOVED("old-moved"),
        NEW_INSTALLED("new-installed"),
        RETURNED("returned");

        private final String label;

        Stage(String label) {
            this.label = label;
        }
    }
}
