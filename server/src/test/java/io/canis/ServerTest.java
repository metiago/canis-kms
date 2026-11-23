package io.canis;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.canis.config.Environment;
import io.canis.models.LoginCredentials;
import io.canis.store.KeyValueStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

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

    @Test
    void startRevalidatesKeyBeforeBindingWithoutLeakingSecrets() throws Exception {
        TlsTestSupport tls = new TlsTestSupport(tempDir);
        Environment base = tls.environment();
        Environment configured = new Environment(
                "PASSWORD_SENTINEL",
                base.username(),
                base.port(),
                Map.of(base.username(), "PASSWORD_SENTINEL"),
                base.secretKeyPath(),
                base.tls());
        Path key = configured.secretKeyPath();
        Path database = tempDir.resolve("configured.db");
        Path otherKey = tempDir.resolve("other.key");
        MasterKeyTestSupport.provision(key);
        MasterKeyTestSupport.provision(otherKey);
        byte[] originalKey = Files.readAllBytes(key);
        KeyValueStore.initialize(database.toString(), key.toString());
        byte[] originalDatabase = Files.readAllBytes(database);

        Server malformed = new Server(configured, database);
        Files.writeString(key, "KEY_CONTENT_SENTINEL_NOT_BASE64!");
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        root.addAppender(appender);
        IllegalStateException failure;
        try {
            failure = assertThrows(IllegalStateException.class, malformed::start);
        } finally {
            root.detachAppender(appender);
            appender.stop();
        }
        assertTrue(failure.getMessage().contains("invalid_format"));
        String emitted = failure.toString()
                + appender.list.stream().map(ILoggingEvent::getFormattedMessage).reduce("", String::concat);
        assertFalse(emitted.contains("KEY_CONTENT_SENTINEL"));
        assertFalse(emitted.contains("PASSWORD_SENTINEL"));
        assertFalse(emitted.contains(key.toString()));
        assertFalse(emitted.contains("TLS server is listening"));
        assertEquals(0, malformed.getPort());

        Files.write(key, originalKey);
        Server changed = new Server(configured, database);
        Files.write(key, Files.readAllBytes(otherKey));
        IllegalStateException mismatch = assertThrows(IllegalStateException.class, changed::start);
        assertEquals("CANIS database key configuration mismatch", mismatch.getMessage());
        assertEquals(0, changed.getPort());

        Files.write(key, originalKey);
        Server missing = new Server(configured, database);
        Files.delete(key);
        IllegalStateException absent = assertThrows(IllegalStateException.class, missing::start);
        assertTrue(absent.getMessage().contains("missing_file"));
        assertEquals(0, missing.getPort());
        assertArrayEquals(originalDatabase, Files.readAllBytes(database));
    }
}
