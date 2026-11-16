package io.canis;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

/** Executable synthetic producer-to-recipient example using the supported TLS client. */
class ReviewerWorkflowTest {
    @TempDir
    Path fixtureDirectory;

    @Test
    void producerLeavesEncryptedFileForRecipientAndOutageBlocksRecovery() throws Exception {
        TlsTestSupport tls = new TlsTestSupport(fixtureDirectory);
        Path masterKey = fixtureDirectory.resolve("master.key");
        Path database = fixtureDirectory.resolve("canis.db");
        MasterKeyTestSupport.provision(masterKey);
        KeyValueStore.initialize(database.toString(), masterKey.toString());
        String producerPassword = randomPassword();
        String recipientPassword = randomPassword();
        Environment serverEnvironment = new Environment(
                recipientPassword,
                "recipient",
                0,
                Map.of("producer", producerPassword, "recipient", recipientPassword),
                masterKey,
                tls.environment().tls());
        Server server = new Server(serverEnvironment, new KeyValueStore(database.toString(), masterKey.toString()));
        Thread listener = new Thread(server::start, "joss03-reviewer-server");
        listener.setDaemon(true);
        listener.start();

        Path sharedDirectory = fixtureDirectory.resolve("shared");
        Path input = fixtureDirectory.resolve("synthetic-input.txt");
        Path envelope = sharedDirectory.resolve("recipient.canis");
        Path output = fixtureDirectory.resolve("recipient-output.txt");
        byte[] fixture = "Synthetic asynchronous file exchange\n".repeat(4).getBytes(StandardCharsets.UTF_8);
        try {
            int port = waitForPort(server);
            Files.createDirectory(sharedDirectory);
            Files.write(input, fixture);
            try (JpawClient recipient = client(tls, port, "recipient", recipientPassword)) {
                assertEquals("OK", recipient.set("recipient"));
            }
            try (JpawClient producer = client(tls, port, "producer", producerPassword)) {
                producer.encryptFile("recipient", input.toFile(), envelope.toFile());
            }
            assertTrue(Files.exists(envelope));
            assertFalse(java.util.Arrays.equals(fixture, Files.readAllBytes(envelope)));
            try (JpawClient recipient = client(tls, port, "recipient", recipientPassword)) {
                recipient.decryptFile("recipient", envelope.toFile(), output.toFile());
            }
            assertArrayEquals(fixture, Files.readAllBytes(output));
            System.out.println("JOSS03_REVIEWER_DEMO=PASS producer->shared-envelope->recipient byte-match");

            server.stop();
            listener.join(5000);
            assertFalse(listener.isAlive(), "TLS listener did not stop");
            assertThrows(IOException.class, () -> client(tls, port, "recipient", recipientPassword));
            System.out.println("JOSS03_OUTAGE_CHECK=PASS new recipient connection rejected after server stop");
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
        long deadline = System.currentTimeMillis() + 5000;
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
