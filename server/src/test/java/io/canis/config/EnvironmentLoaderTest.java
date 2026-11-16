package io.canis.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class EnvironmentLoaderTest {

    @Test
    void testLoadEnvironment() {
        Environment environment = EnvironmentLoader.loadEnvironment(validEnvironment());

        assertEquals("admin", environment.username());
        assertEquals("123", environment.password());
        assertEquals(3307, environment.port());
        assertEquals(Path.of("secret.txt"), environment.secretKeyPath());
        assertEquals("123", environment.serviceCredentials().get("admin"));
    }

    @Test
    void testLoadEnvironmentWithServiceCredentials() {
        Map<String, String> rawEnvironment = validEnvironment();
        rawEnvironment.put("CANIS_SERVICE_CREDENTIALS", "serviceA:secret-a, serviceB:secret:b:with:colons");

        Environment environment = EnvironmentLoader.loadEnvironment(rawEnvironment);

        assertEquals("secret-a", environment.serviceCredentials().get("serviceA"));
        assertEquals("secret:b:with:colons", environment.serviceCredentials().get("serviceB"));
        assertEquals("123", environment.serviceCredentials().get("admin"));
    }

    @Test
    void testLoadEnvironmentRejectsInvalidServiceCredentials() {
        Map<String, String> environment = validEnvironment();
        String sensitiveValue = "configuration-secret-sentinel";
        environment.put("CANIS_SERVICE_CREDENTIALS", ":" + sensitiveValue);

        IllegalStateException failure =
                assertThrows(IllegalStateException.class, () -> EnvironmentLoader.loadEnvironment(environment));
        assertFalse(failure.toString().contains(sensitiveValue));
    }

    @Test
    void testLoadEnvironmentRejectsMissingRequiredValue() {
        Map<String, String> environment = validEnvironment();
        environment.remove("CANIS_SECRET_KEY");

        assertThrows(IllegalStateException.class, () -> EnvironmentLoader.loadEnvironment(environment));
    }

    @Test
    void testLoadEnvironmentRejectsInvalidMasterKeyPathWithoutEchoingIt() {
        Map<String, String> environment = validEnvironment();
        String invalid = "secret" + (char) 0 + "path";
        environment.put("CANIS_SECRET_KEY", invalid);

        IllegalStateException failure =
                assertThrows(IllegalStateException.class, () -> EnvironmentLoader.loadEnvironment(environment));
        assertFalse(failure.toString().contains(invalid));
        assertEquals("Invalid master key path", failure.getMessage());
    }

    @Test
    void testLoadEnvironmentRejectsInvalidPort() {
        Map<String, String> environment = validEnvironment();
        environment.put("CANIS_PORT", "70000");

        assertThrows(IllegalStateException.class, () -> EnvironmentLoader.loadEnvironment(environment));
    }

    @Test
    void testLoadEnvironmentRejectsMissingTlsSettings() {
        Map<String, String> environment = validEnvironment();
        environment.remove("CANIS_TLS_KEYSTORE");
        assertThrows(IllegalStateException.class, () -> EnvironmentLoader.loadEnvironment(environment));
    }

    @Test
    void testLoadEnvironmentRejectsBlankTlsPassword() {
        Map<String, String> environment = validEnvironment();
        environment.put("CANIS_TLS_TRUSTSTORE_PASSWORD", " ");
        assertThrows(IllegalStateException.class, () -> EnvironmentLoader.loadEnvironment(environment));
    }

    private Map<String, String> validEnvironment() {
        Map<String, String> environment = new HashMap<>();
        environment.put("CANIS_USERNAME", "admin");
        environment.put("CANIS_PASSWORD", "123");
        environment.put("CANIS_SECRET_KEY", "secret.txt");
        environment.put("CANIS_PORT", "3307");
        environment.put("CANIS_TLS_KEYSTORE", "server.p12");
        environment.put("CANIS_TLS_KEYSTORE_PASSWORD", UUID.randomUUID().toString());
        environment.put("CANIS_TLS_TRUSTSTORE", "clients.p12");
        environment.put("CANIS_TLS_TRUSTSTORE_PASSWORD", UUID.randomUUID().toString());
        return environment;
    }
}
