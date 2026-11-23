package io.canis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.canis.config.TlsConfiguration;
import io.canis.config.TlsContextFactory;
import io.canis.store.KeyValueStore;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

class ServerTlsSecurityTest {
    @TempDir
    Path tempDir;

    private TlsTestSupport tls;
    private Server server;
    private Thread serverThread;

    @BeforeEach
    void start() throws Exception {
        tls = new TlsTestSupport(tempDir);
        MasterKeyTestSupport.provision(tempDir.resolve("secret.key"));
        KeyValueStore.initialize(
                tempDir.resolve("db.dat").toString(),
                tempDir.resolve("secret.key").toString());
        server = new Server(
                tls.environment(),
                new KeyValueStore(
                        tempDir.resolve("db.dat").toString(),
                        tempDir.resolve("secret.key").toString()));
        serverThread = new Thread(server::start);
        serverThread.start();
        long deadline = System.currentTimeMillis() + 3000;
        while (server.getPort() == 0 && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertTrue(server.getPort() > 0);
    }

    @AfterEach
    void stop() throws Exception {
        if (server != null) {
            server.stop();
            serverThread.join(2000);
        }
    }

    @Test
    void rejectsPlaintextBeforeLogin() throws Exception {
        try (Socket socket = new Socket("localhost", server.getPort())) {
            socket.setSoTimeout(3000);
            socket.getOutputStream().write("|login admin:123\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            socket.shutdownOutput();
            // A CANISP response starts with a small positive length, whose first byte is zero.
            assertNotEquals(0, socket.getInputStream().read());
        }
    }

    @Test
    void rejectsTls12() throws Exception {
        try (SSLSocket socket =
                (SSLSocket) tls.clientContext().getSocketFactory().createSocket("localhost", server.getPort())) {
            socket.setSoTimeout(3000);
            socket.setEnabledProtocols(new String[] {"TLSv1.2"});
            assertThrows(IOException.class, socket::startHandshake);
        }
    }

    @Test
    void negotiatesEveryProviderEnabledApprovedTls13Suite(TestReporter reporter) throws Exception {
        String[] approved = {"TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384", "TLS_CHACHA20_POLY1305_SHA256"};
        int tested = 0;
        var providerDisabled = new ArrayList<String>();
        for (String suite : approved) {
            try (SSLSocket socket = tls.connect(server.getPort())) {
                if (!Arrays.asList(socket.getEnabledCipherSuites()).contains(suite)) {
                    providerDisabled.add(suite);
                    continue;
                }
                socket.setEnabledCipherSuites(new String[] {suite});
                socket.startHandshake();
                assertEquals("TLSv1.3", socket.getSession().getProtocol());
                assertEquals(suite, socket.getSession().getCipherSuite());
                tested++;
            }
        }
        reporter.publishEntry(
                "providerDisabledSuites", providerDisabled.isEmpty() ? "none" : String.join(",", providerDisabled));
        assertTrue(tested > 0);
    }

    @Test
    void rejectsUntrustedClientCertificate() throws Exception {
        try (SSLSocket socket = (SSLSocket)
                tls.untrustedClientContext().getSocketFactory().createSocket("localhost", server.getPort())) {
            socket.setSoTimeout(3000);
            socket.setEnabledProtocols(new String[] {"TLSv1.3"});
            assertThrows(IOException.class, () -> {
                socket.startHandshake();
                new PrintWriter(socket.getOutputStream(), true).println("|login admin:123");
                new DataInputStream(socket.getInputStream()).readInt();
            });
        }
    }

    @Test
    void rejectsInvalidCredentialsAndCommandsBeforeLogin() throws Exception {
        assertEquals("Authentication failed", login("|login admin:wrong"));
        assertEquals("Authentication failed", login("|health"));
    }

    @Test
    void failedAuthenticationDoesNotLogClientSuppliedFields() throws Exception {
        Logger serverLogger = (Logger) LoggerFactory.getLogger(Server.class);
        Logger auditLogger = (Logger) LoggerFactory.getLogger("io.canis.audit");
        ListAppender<ILoggingEvent> serverAppender = new ListAppender<>();
        ListAppender<ILoggingEvent> auditAppender = new ListAppender<>();
        serverAppender.start();
        auditAppender.start();
        serverLogger.addAppender(serverAppender);
        auditLogger.addAppender(auditAppender);

        List<String> usernames = List.of(
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKgw" + "A".repeat(512) + "PRIVATE_KEY_MARKER",
                "LONG_USERNAME_MARKER" + "X".repeat(8192),
                "CONTROL_MARKER\u000b\u001b[31m\tTAIL",
                "INJECTION_MARKER event=auth_success identity=admin reason=ok");
        try {
            for (String username : usernames) {
                assertEquals("Authentication failed", login("|login " + username + ":PASSWORD_MARKER"));
            }
            assertEquals("Invalid input format", login("|login MALFORMED_MARKER"));
            assertEquals("Authentication failed", login("|health UNKNOWN_COMMAND_MARKER"));

            String logged = serverAppender.list.stream()
                            .map(ILoggingEvent::getFormattedMessage)
                            .reduce("", String::concat)
                    + auditAppender.list.stream()
                            .map(ILoggingEvent::getFormattedMessage)
                            .reduce("", String::concat);
            for (String marker : List.of(
                    "PRIVATE_KEY_MARKER",
                    "LONG_USERNAME_MARKER",
                    "CONTROL_MARKER",
                    "INJECTION_MARKER",
                    "PASSWORD_MARKER",
                    "MALFORMED_MARKER",
                    "UNKNOWN_COMMAND_MARKER")) {
                assertFalse(logged.contains(marker));
            }
            assertEquals(6, auditAppender.list.size());
            assertTrue(auditAppender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .allMatch(message -> message.startsWith("event=auth_failure identity=unknown remote=")));

            assertEquals("Authentication successful", login("|login admin:123"));
            assertTrue(auditAppender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .anyMatch(message -> message.startsWith("event=auth_success identity=admin remote=")));
        } finally {
            serverLogger.detachAppender(serverAppender);
            auditLogger.detachAppender(auditAppender);
            serverAppender.stop();
            auditAppender.stop();
        }
    }

    @Test
    void invalidTlsMaterialPreventsStartup() throws Exception {
        TlsConfiguration config = tls.environment().tls();
        assertThrows(
                IllegalStateException.class,
                () -> TlsContextFactory.create(new TlsConfiguration(
                        tempDir.resolve("missing.p12"),
                        config.keyStorePassword(),
                        config.trustStore(),
                        config.trustStorePassword())));
        assertThrows(
                IllegalStateException.class,
                () -> TlsContextFactory.create(new TlsConfiguration(
                        config.keyStore(), "wrong-password", config.trustStore(), config.trustStorePassword())));
        Path malformed = tempDir.resolve("malformed-trust.p12");
        Files.write(malformed, new byte[] {1, 2, 3, 4});
        assertThrows(
                IllegalStateException.class,
                () -> TlsContextFactory.create(new TlsConfiguration(
                        config.keyStore(), config.keyStorePassword(), malformed, config.trustStorePassword())));
    }

    @Test
    void databaseTamperingStillFailsClosed() throws Exception {
        Path database = tempDir.resolve("db.dat");
        Path key = tempDir.resolve("secret.key");
        KeyValueStore store = new KeyValueStore(database.toString(), key.toString());
        store.create("sample");
        byte[] bytes = Files.readAllBytes(database);
        bytes[bytes.length - 1] ^= 1;
        Files.write(database, bytes);
        assertThrows(IllegalStateException.class, () -> new KeyValueStore(database.toString(), key.toString()));
    }

    private String login(String command) throws Exception {
        try (SSLSocket socket = tls.connect(server.getPort())) {
            new PrintWriter(socket.getOutputStream(), true).println(command);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] payload = new byte[in.readInt()];
            in.readFully(payload);
            return new String(payload, java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
