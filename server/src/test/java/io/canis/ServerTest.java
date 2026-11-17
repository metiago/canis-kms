package io.canis;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.canis.config.Environment;
import io.canis.models.LoginCredentials;
import io.canis.store.KeyValueStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ServerTest {
    @TempDir
    Path tempDir;

    @Test
    void testParseLoginCredentials() {
        Optional<LoginCredentials> credentials = Server.parseLoginCredentials("|login serviceA:secret");

        assertTrue(credentials.isPresent());
        assertEquals("serviceA", credentials.get().username());
        assertEquals("secret", credentials.get().password());
    }

    @Test
    void testParseLoginCredentialsAllowsColonInPassword() {
        Optional<LoginCredentials> credentials = Server.parseLoginCredentials("|login serviceA:secret:with:colons");

        assertTrue(credentials.isPresent());
        assertEquals("serviceA", credentials.get().username());
        assertEquals("secret:with:colons", credentials.get().password());
    }

    @Test
    void testParseLoginCredentialsRejectsMissingCredentials() {
        assertFalse(Server.parseLoginCredentials(null).isPresent());
        assertFalse(Server.parseLoginCredentials("|login").isPresent());
        assertFalse(Server.parseLoginCredentials("|login ").isPresent());
        assertFalse(Server.parseLoginCredentials("|login serviceA").isPresent());
    }

    @Test
    void testParseLoginCredentialsRejectsEmptyUsernameOrPassword() {
        assertFalse(Server.parseLoginCredentials("|login :secret").isPresent());
        assertFalse(Server.parseLoginCredentials("|login serviceA:").isPresent());
        assertFalse(Server.parseLoginCredentials("|login serviceA:   ").isPresent());
    }

    @Test
    void testParseLoginCredentialsRejectsInvalidLoginCommand() {
        assertFalse(Server.parseLoginCredentials("|health").isPresent());
        assertFalse(Server.parseLoginCredentials("|loginserviceA:secret").isPresent());
    }

    @Test
    void testStopClosesServerLoop() throws Exception {
        MasterKeyTestSupport.provision(tempDir.resolve("secret.key"));
        KeyValueStore.initialize(
                tempDir.resolve("db.dat").toString(),
                tempDir.resolve("secret.key").toString());
        Server server = new Server(
                new TlsTestSupport(tempDir).environment(),
                new KeyValueStore(
                        tempDir.resolve("db.dat").toString(),
                        tempDir.resolve("secret.key").toString()));
        Thread serverThread = new Thread(server::start);
        serverThread.start();

        long deadline = System.currentTimeMillis() + 3000;
        while (server.getPort() == 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }

        assertTrue(server.getPort() > 0);
        server.stop();
        serverThread.join(2000);

        assertFalse(serverThread.isAlive());
    }

    @Test
    void masterKeyIsRequiredBeforeServerConstructionAndBinding() throws Exception {
        TlsTestSupport tls = new TlsTestSupport(tempDir);
        Path database = tempDir.resolve("fresh.db");
        Path key = tls.environment().secretKeyPath();

        assertThrows(IllegalStateException.class, () -> new Server(tls.environment(), database));
        assertFalse(Files.exists(key));
        assertFalse(Files.exists(database));

        MasterKeyTestSupport.provision(key);
        Files.writeString(key, "invalid base64!");
        assertThrows(IllegalStateException.class, () -> new Server(tls.environment(), database));
        assertFalse(Files.exists(database));
    }

    @Test
    void missingDatabaseStopsConstructionAndInjectedStoreRequiresConfiguredKey() throws Exception {
        TlsTestSupport tls = new TlsTestSupport(tempDir);
        Path database = tempDir.resolve("fresh.db");
        Path key = tls.environment().secretKeyPath();
        MasterKeyTestSupport.provision(key);

        assertThrows(IllegalStateException.class, () -> new Server(tls.environment(), database));
        assertFalse(Files.exists(database));

        KeyValueStore.initialize(database.toString(), key.toString());
        byte[] initialized = Files.readAllBytes(database);
        byte[] tampered = initialized.clone();
        tampered[tampered.length - 1] ^= 1;
        Files.write(database, tampered);
        assertThrows(IllegalStateException.class, () -> new Server(tls.environment(), database));
        assertArrayEquals(tampered, Files.readAllBytes(database));

        Files.write(database, initialized);
        KeyValueStore store = new KeyValueStore(database.toString(), key.toString());
        Files.delete(key);
        assertThrows(IllegalStateException.class, () -> new Server(tls.environment(), store));
        assertTrue(Files.exists(database));
    }

    @Test
    void bothServerConstructionPathsRejectMismatchedOrChangedMasterKeys() throws Exception {
        TlsTestSupport tls = new TlsTestSupport(tempDir);
        Environment base = tls.environment();
        Path database = tempDir.resolve("configured.db");
        Path key = base.secretKeyPath();
        Path otherKey = tempDir.resolve("other.key");
        MasterKeyTestSupport.provision(key);
        MasterKeyTestSupport.provision(otherKey);
        KeyValueStore.initialize(database.toString(), key.toString());
        byte[] original = Files.readAllBytes(database);
        KeyValueStore loaded = new KeyValueStore(database.toString(), key.toString());
        Environment mismatched = new Environment(
                base.password(), base.username(), base.port(), base.serviceCredentials(), otherKey, base.tls());

        assertThrows(IllegalStateException.class, () -> new Server(mismatched, loaded));
        assertThrows(IllegalStateException.class, () -> new Server(mismatched, database));
        Files.writeString(key, "invalid-base64!");
        assertThrows(IllegalStateException.class, () -> new Server(base, loaded));
        assertThrows(IllegalStateException.class, () -> new Server(base, database));
        assertArrayEquals(original, Files.readAllBytes(database));
    }
}
