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
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Provides a reproducible review of CANIS encrypted file exchange: a sender writes an encrypted file to shared storage,
 * a recipient recovers the original through CANIS, and server shutdown prevents a new key recovery connection.
 */
class ReviewerWorkflowTest {
    @TempDir
    Path fixtureDirectory;

    @Test
    void senderSharesEncryptedFileRecipientRecoversKeyAndServerOutageBlocksNewConnection() throws Exception {
        var exchange = startServer();
        int port;
        try {
            port = waitForPort(exchange.server());
            var files = prepareFiles();

            try (var recipient = client(exchange.tls(), port, "recipient", exchange.recipientPassword())) {
                assertEquals("OK", recipient.set("recipient"), "Recipient key should be created");
            }
            try (var sender = client(exchange.tls(), port, "sender", exchange.senderPassword())) {
                sender.encryptFile(
                        "recipient",
                        files.input().toFile(),
                        files.encryptedFile().toFile());
            }

            assertTrue(Files.exists(files.encryptedFile()), "Sender should place the encrypted file in shared storage");
            assertFalse(
                    Arrays.equals(files.plaintext(), Files.readAllBytes(files.encryptedFile())),
                    "Encrypted file should differ from the plaintext file");

            try (var recipient = client(exchange.tls(), port, "recipient", exchange.recipientPassword())) {
                recipient.decryptFile(
                        "recipient",
                        files.encryptedFile().toFile(),
                        files.output().toFile());
            }
            assertArrayEquals(
                    files.plaintext(),
                    Files.readAllBytes(files.output()),
                    "Recipient should recover the original file");
        } finally {
            exchange.stop();
        }
        assertFalse(exchange.listener().isAlive(), "CANIS server listener should stop");
        assertThrows(
                IOException.class,
                () -> client(exchange.tls(), port, "recipient", exchange.recipientPassword()),
                "Recipient should not reach key recovery after the CANIS server stops");
    }

    private ExchangeServer startServer() throws Exception {
        var tls = new TlsTestSupport(fixtureDirectory);
        var masterKey = fixtureDirectory.resolve("master.key");
        var database = fixtureDirectory.resolve("canis.db");
        MasterKeyTestSupport.provision(masterKey);
        KeyValueStore.initialize(database.toString(), masterKey.toString());

        var senderPassword = randomPassword();
        var recipientPassword = randomPassword();
        var environment = new Environment(
                recipientPassword,
                "recipient",
                0,
                Map.of("sender", senderPassword, "recipient", recipientPassword),
                masterKey,
                tls.environment().tls());
        var server = new Server(environment, new KeyValueStore(database.toString(), masterKey.toString()));
        var listener = Thread.ofPlatform()
                .name("canis-file-exchange-server")
                .daemon(true)
                .start(server::start);
        return new ExchangeServer(tls, server, listener, senderPassword, recipientPassword);
    }

    private ExchangeFiles prepareFiles() throws IOException {
        var sharedStorage = fixtureDirectory.resolve("shared-storage");
        Files.createDirectory(sharedStorage);
        var input = fixtureDirectory.resolve("sender-input.txt");
        var plaintext = "CANIS encrypted file exchange\n".repeat(4).getBytes(StandardCharsets.UTF_8);
        Files.write(input, plaintext);
        return new ExchangeFiles(
                input,
                sharedStorage.resolve("recipient.canis"),
                fixtureDirectory.resolve("recipient-output.txt"),
                plaintext);
    }

    private JpawClient client(TlsTestSupport tls, int port, String identity, String password) throws IOException {
        return new JpawClient(new io.canis.jpaw.pojo.Environment(
                "localhost", port, identity, password, tls.clientTlsConfiguration()));
    }

    private int waitForPort(Server server) throws InterruptedException {
        var deadline = Instant.now().plus(Duration.ofSeconds(5));
        while (server.getPort() == 0 && Instant.now().isBefore(deadline)) {
            Thread.sleep(Duration.ofMillis(10));
        }
        var port = server.getPort();
        assertTrue(port > 0, "CANIS server should bind a TLS port");
        return port;
    }

    private String randomPassword() {
        var random = new byte[24];
        new SecureRandom().nextBytes(random);
        return HexFormat.of().formatHex(random);
    }

    private record ExchangeFiles(Path input, Path encryptedFile, Path output, byte[] plaintext) {}

    private record ExchangeServer(
            TlsTestSupport tls, Server server, Thread listener, String senderPassword, String recipientPassword) {
        void stop() throws InterruptedException {
            server.stop();
            listener.join(Duration.ofSeconds(5));
        }
    }
}
