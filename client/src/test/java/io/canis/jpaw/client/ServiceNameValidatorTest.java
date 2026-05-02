package io.canis.jpaw.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ServiceNameValidatorTest {

  @Test
  void testAcceptsValidServiceNames() {
    String[] validNames = {
        "serviceA",
        "service-a",
        "service_a",
        "service.a",
        "svc123",
        "a".repeat(128)
    };

    for (String serviceName : validNames) {
      assertEquals(serviceName, ServiceNameValidator.requireValid(serviceName));
    }
  }

  @Test
  void testRejectsInvalidServiceNames() {
    String[] invalidNames = {
        "",
        " ",
        "service A",
        "service\tA",
        "service\nA",
        "service|A",
        "service:A",
        "a".repeat(129)
    };

    assertThrows(IllegalArgumentException.class, () -> ServiceNameValidator.requireValid(null));
    for (String serviceName : invalidNames) {
      assertThrows(IllegalArgumentException.class,
          () -> ServiceNameValidator.requireValid(serviceName));
    }
  }
}
