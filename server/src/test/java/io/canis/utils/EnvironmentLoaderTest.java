package io.canis.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.canis.models.Environment;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class EnvironmentLoaderTest {

  @Test
  void testLoadEnvironment() {
    Environment environment = EnvironmentLoader.loadEnvironment(validEnvironment());

    assertEquals("admin", environment.username());
    assertEquals("123", environment.password());
    assertEquals(3307, environment.port());
  }

  @Test
  void testLoadEnvironmentRejectsMissingRequiredValue() {
    Map<String, String> environment = validEnvironment();
    environment.remove("CANIS_SECRET_KEY");

    assertThrows(IllegalStateException.class,
        () -> EnvironmentLoader.loadEnvironment(environment));
  }

  @Test
  void testLoadEnvironmentRejectsInvalidPort() {
    Map<String, String> environment = validEnvironment();
    environment.put("CANIS_PORT", "70000");

    assertThrows(IllegalStateException.class,
        () -> EnvironmentLoader.loadEnvironment(environment));
  }

  private Map<String, String> validEnvironment() {
    Map<String, String> environment = new HashMap<>();
    environment.put("CANIS_USERNAME", "admin");
    environment.put("CANIS_PASSWORD", "123");
    environment.put("CANIS_SECRET_KEY", "secret.txt");
    environment.put("CANIS_PORT", "3307");
    return environment;
  }
}
