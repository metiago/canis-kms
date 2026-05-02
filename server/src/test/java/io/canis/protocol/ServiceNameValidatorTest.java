package io.canis.protocol;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
      assertTrue(ServiceNameValidator.isValid(serviceName));
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

    assertFalse(ServiceNameValidator.isValid(null));
    for (String serviceName : invalidNames) {
      assertFalse(ServiceNameValidator.isValid(serviceName));
    }
  }
}
