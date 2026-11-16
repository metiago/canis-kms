package io.canis;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.canis.config.Environment;
import io.canis.jpaw.client.JpawClient;
import io.canis.store.KeyValueStore;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Reproduces the current credential-level shared trust boundary with synthetic data. */
class SharedTrustDomainResearchTest {

    @TempDir
    Path tempDir;

    @Test
    void authenticatedPeerCanDecryptAnotherServicesFileButInvalidCredentialsCannotConnect() throws Exception {
        TlsTestSupport tls = new TlsTestSupport(tempDir);
        Path masterKey = tempDir.resolve("master.key");
        Path database = tempDir.resolve("canis.db");
        MasterKeyTestSupport.provision(masterKey);
        KeyValueStore.initialize(database.toString(), masterKey.toString());
        KeyValueStore store = new KeyValueStore(database.toString(), masterKey.toString());

        String recipientPassword = randomPassword();
        String producerPassword = randomPassword();
        String observerPassword = randomPassword();
        Environment base = tls.environment();
        Environment environment = new Environment(
                recipientPassword,
                "recipient",
                0,
                Map.of(
                        "recipient", recipientPassword,
                        "producer", producerPassword,
                        "observer", observerPassword),
                masterKey,
                base.tls());
        Server server = new Server(environment, store);
        Thread listener = new Thread(server::start);
        listener.setDaemon(true);
        listener.start();

        try {
            int port = waitForPort(server);
            Path input = tempDir.resolve("synthetic.txt");
            Path envelope = tempDir.resolve("synthetic.canis");
            Path recipientOutput = tempDir.resolve("recipient.txt");
            Path observerOutput = tempDir.resolve("observer.txt");
            byte[] fixture = "Synthetic CANIS research fixture\n".repeat(8).getBytes(StandardCharsets.UTF_8);
            Files.write(input, fixture);

            try (JpawClient recipient = client(tls, port, "recipient", recipientPassword);
                    JpawClient producer = client(tls, port, "producer", producerPassword);
                    JpawClient observer = client(tls, port, "observer", observerPassword)) {
                assertEquals("OK", recipient.set("recipient"));
                producer.encryptFile("recipient", input.toFile(), envelope.toFile());
                recipient.decryptFile("recipient", envelope.toFile(), recipientOutput.toFile());
                observer.decryptFile("recipient", envelope.toFile(), observerOutput.toFile());
            }

            assertArrayEquals(fixture, Files.readAllBytes(recipientOutput));
            assertArrayEquals(fixture, Files.readAllBytes(observerOutput));
            String invalidPassword = (observerPassword.charAt(0) == '0' ? '1' : '0') + observerPassword.substring(1);
            assertThrows(IOException.class, () -> client(tls, port, "observer", invalidPassword));
        } finally {
            server.stop();
            listener.join(5000);
        }
    }

    private JpawClient client(TlsTestSupport tls, int port, String identity, String password) throws IOException {
        return new JpawClient(new io.canis.jpaw.pojo.Environment(
                "localhost", port, identity, password, tls.clientTlsConfiguration()));
    }

    private int waitForPort(Server server) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 3000;
        while (server.getPort() == 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        int port = server.getPort();
        assertTrue(port > 0, "TLS listener did not bind");
        return port;
    }

    private String randomPassword() {
        byte[] random = new byte[24];
        new SecureRandom().nextBytes(random);
        return HexFormat.of().formatHex(random);
    }
}
