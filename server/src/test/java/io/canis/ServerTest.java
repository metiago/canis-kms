package io.canis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
