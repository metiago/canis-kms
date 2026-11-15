package io.canis.protocol;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class ServiceNameValidatorTest {

    @ParameterizedTest
    @MethodSource("validServiceNames")
    void testAcceptsValidServiceNames(String serviceName) {
        assertTrue(ServiceNameValidator.isValid(serviceName));
    }

    static Stream<String> validServiceNames() {
        return Stream.of("serviceA", "service-a", "service_a", "service.a", "svc123", "a".repeat(128));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("invalidServiceNames")
    void testRejectsInvalidServiceNames(String serviceName) {
        assertFalse(ServiceNameValidator.isValid(serviceName));
    }

    static Stream<String> invalidServiceNames() {
        return Stream.of(" ", "service A", "service\tA", "service\nA", "service|A", "service:A", "a".repeat(129));
    }
}
