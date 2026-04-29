package io.canis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.canis.models.LoginCredentials;
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
}
