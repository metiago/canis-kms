package io.canis.jpaw.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.canis.jpaw.pojo.Environment;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class EnvironmentLoaderTest {

  @Test
  void testLoadEnvironment() {
    Environment environment = EnvironmentLoader.loadEnvironment(validEnvironment());

    assertEquals("admin", environment.getUsername());
    assertEquals("123", environment.getPassword());
    assertEquals(3307, environment.getPort());
  }

  @Test
  void testLoadEnvironmentRejectsMissingRequiredValue() {
    Map<String, String> environment = validEnvironment();
    environment.remove("CANIS_SERVER_PORT");

    assertThrows(IllegalStateException.class,
        () -> EnvironmentLoader.loadEnvironment(environment));
  }

  @Test
  void testLoadEnvironmentRejectsInvalidPort() {
    Map<String, String> environment = validEnvironment();
    environment.put("CANIS_SERVER_PORT", "zero");

    assertThrows(IllegalStateException.class,
        () -> EnvironmentLoader.loadEnvironment(environment));
  }

  private Map<String, String> validEnvironment() {
    Map<String, String> environment = new HashMap<>();
    environment.put("CANIS_USERNAME", "admin");
    environment.put("CANIS_PASSWORD", "123");
    environment.put("CANIS_SERVER_PORT", "3307");
    return environment;
  }
}
