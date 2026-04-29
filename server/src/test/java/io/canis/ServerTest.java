package io.canis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.canis.models.Environment;
import io.canis.models.LoginCredentials;
import io.canis.store.KeyValueStore;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class ServerTest {

  @Test
  void testParseLoginCredentials() {
    Optional<LoginCredentials> credentials =
        Server.parseLoginCredentials("|login serviceA:secret");

    assertTrue(credentials.isPresent());
    assertEquals("serviceA", credentials.get().username());
    assertEquals("secret", credentials.get().password());
  }

  @Test
  void testParseLoginCredentialsAllowsColonInPassword() {
    Optional<LoginCredentials> credentials =
        Server.parseLoginCredentials("|login serviceA:secret:with:colons");

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
    Environment environment = new Environment("123", "admin", 0, Map.of("admin", "123"));
    Server server = new Server(environment, mock(KeyValueStore.class));
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
}
