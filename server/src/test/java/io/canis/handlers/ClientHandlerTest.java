package io.canis.handlers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.canis.MasterKeyTestSupport;
import io.canis.crypto.Cryptographer;
import io.canis.crypto.SymmetricGenerator;
import io.canis.net.BoundedLineReader;
import io.canis.protocol.ProtocolError;
import io.canis.store.Entry;
import io.canis.store.KeyValueStore;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

public class ClientHandlerTest {

    @TempDir
    Path tempDir;

    @Test
    void testVersionCommandReturnsCanispVersion() throws Exception {
        KeyValueStore store = mock(KeyValueStore.class);

        Socket socket = mock(Socket.class);
        when(socket.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 3307));

        ByteArrayOutputStream response = new ByteArrayOutputStream();
        ClientHandler handler = new ClientHandler(
                socket, new BufferedReader(new StringReader("|version")), new DataOutputStream(response), store);

        handler.run();

        assertEquals("|s>CANISP/1", readResponse(response.toByteArray()));
    }

    @Test
    void testOversizedCommandIsRejected() throws Exception {
        KeyValueStore store = mock(KeyValueStore.class);

        Socket socket = mock(Socket.class);
        when(socket.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 3307));

        String oversizedCommand = "x".repeat(BoundedLineReader.MAX_LINE_CHARS + 1);
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        ClientHandler handler = new ClientHandler(
                socket, new BufferedReader(new StringReader(oversizedCommand)), new DataOutputStream(response), store);

        handler.run();

        assertEquals(stringResponse(ProtocolError.REQUEST_TOO_LARGE), readResponse(response.toByteArray()));
    }

    @Test
    void testDecryptCommandUsesStoredPrivateKey() throws Exception {
        MasterKeyTestSupport.provision(tempDir.resolve("secret.key"));
        KeyValueStore.initialize(
                tempDir.resolve("db.dat").toString(),
                tempDir.resolve("secret.key").toString());
        KeyValueStore store = new KeyValueStore(
                tempDir.resolve("db.dat").toString(),
                tempDir.resolve("secret.key").toString());
        store.create("serviceA");
        String privateKey = storedPrivateKey(tempDir.resolve("db.dat"), tempDir.resolve("secret.key"));
        byte[] publicKeyBytes = Base64.getDecoder().decode(store.get("serviceA").publicKey());
        var publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        byte[] plaintext = "file-key".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = Cryptographer.encrypt(plaintext, publicKey);

        Socket socket = mock(Socket.class);
        when(socket.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 3307));

        String command = "|decrypt serviceA " + Base64.getEncoder().encodeToString(encrypted);
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        Logger auditLogger = (Logger) LoggerFactory.getLogger("io.canis.audit");
        ListAppender<ILoggingEvent> auditAppender = new ListAppender<>();
        auditAppender.start();
        auditLogger.addAppender(auditAppender);
        ClientHandler handler = new ClientHandler(
                socket,
                new BufferedReader(new StringReader(command)),
                new DataOutputStream(response),
                store,
                "serviceB");

        try {
            handler.run();
        } finally {
            auditLogger.detachAppender(auditAppender);
            auditAppender.stop();
        }

        String message = readResponse(response.toByteArray());
        String decryptedBase64 = message.replace("|s>", "");

        assertArrayEquals(plaintext, Base64.getDecoder().decode(decryptedBase64));
        assertTrue(auditAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(event -> event.contains("event=decrypt_success identity=serviceB key=serviceA")));
        assertFalse(auditAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(event -> event.contains(privateKey)));
        assertFalse(message.contains(privateKey));
        assertFalse(runCommand("|get serviceA", store).contains(privateKey));
        assertFalse(runCommand("|get-public serviceA", store).contains(privateKey));
        assertFalse(runCommand("|list", store).contains(privateKey));
    }

    @Test
    void testListCommandUsesStoredEntries() throws Exception {
        KeyValueStore store = mock(KeyValueStore.class);
        when(store.list())
                .thenReturn(List.of(new Entry("serviceA", "public-key-a"), new Entry("serviceB", "public-key-b")));

        Socket socket = mock(Socket.class);
        when(socket.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 3307));

        ByteArrayOutputStream response = new ByteArrayOutputStream();
        ClientHandler handler = new ClientHandler(
                socket, new BufferedReader(new StringReader("|list")), new DataOutputStream(response), store);

        handler.run();

        String message = readResponse(response.toByteArray());

        assertTrue(message.startsWith("|a>"));
        assertTrue(message.contains("|ms>name:serviceA"));
        assertTrue(message.contains("|ms>publicKey:public-key-a"));
        assertTrue(message.contains("|ms>name:serviceB"));
        assertTrue(message.contains("|ms>publicKey:public-key-b"));
        assertFalse(message.contains("private-key-a"));
        assertFalse(message.contains("private-key-b"));
        verify(store).list();
    }

    @Test
    void testGetPublicCommandReturnsOnlyPublicKey() throws Exception {
        KeyValueStore store = mock(KeyValueStore.class);
        when(store.get("serviceA")).thenReturn(new Entry("serviceA", "public-key-a"));

        Socket socket = mock(Socket.class);
        when(socket.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 3307));

        ByteArrayOutputStream response = new ByteArrayOutputStream();
        ClientHandler handler = new ClientHandler(
                socket,
                new BufferedReader(new StringReader("|get-public serviceA")),
                new DataOutputStream(response),
                store);

        handler.run();

        String message = readResponse(response.toByteArray());

        assertTrue(message.startsWith("|s>"));
        assertTrue(message.contains("public-key-a"));
        assertFalse(message.contains("private-key-a"));
    }

    @Test
    void testGetMissingKeyReturnsKeyNotFound() throws Exception {
        KeyValueStore store = mock(KeyValueStore.class);

        assertEquals(stringResponse(ProtocolError.KEY_NOT_FOUND), runCommand("|get missing", store));
        verify(store).get("missing");
    }

    @Test
    void testGetExistingKeyReturnsMetadataWithoutPrivateKey() throws Exception {
        KeyValueStore store = mock(KeyValueStore.class);
        when(store.get("serviceA")).thenReturn(new Entry("serviceA", "public-key-a"));

        String response = runCommand("|get serviceA", store);

        assertTrue(response.contains("|ms>name:serviceA"));
        assertTrue(response.contains("|ms>publicKey:public-key-a"));
        assertFalse(response.contains("private-key-a"));
    }

    @Test
    void testSetRejectsDuplicateAndPreservesExistingEntry() throws Exception {
        Entry existing = new Entry("serviceA", "original-public");
        KeyValueStore store = mock(KeyValueStore.class);
        when(store.get("serviceA")).thenReturn(existing);

        String message = runCommand("|set serviceA", store);

        assertEquals("|s>ERROR: Service already exists", message);
        verify(store).get("serviceA");
        verifyNoInteractionsAfterGet(store);
    }

    @Test
    void testSetCreatesEntryWhenServiceDoesNotExist() throws Exception {
        KeyValueStore store = mock(KeyValueStore.class);
        when(store.create("serviceA")).thenReturn(true);

        String message = runCommand("|set serviceA", store);

        assertEquals("|s>OK", message);
        verify(store).create("serviceA");
    }

    @Test
    void testDeleteReturnsOkAndRecordsAuditEvent() throws Exception {
        KeyValueStore store = mock(KeyValueStore.class);
        Logger auditLogger = (Logger) LoggerFactory.getLogger("io.canis.audit");
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        auditLogger.addAppender(appender);

        try {
            assertEquals("|s>OK", runCommand("|del serviceA", store));
            verify(store).delete("serviceA");
            assertTrue(appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .anyMatch(message -> message.contains("event=key_deleted") && message.contains("key=serviceA")));
        } finally {
            auditLogger.detachAppender(appender);
            appender.stop();
        }
    }

    private void verifyNoInteractionsAfterGet(KeyValueStore store) throws Exception {
        org.mockito.Mockito.verify(store, org.mockito.Mockito.never()).create(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void testInvalidServiceNamesAreRejectedBeforeStoreAccess() throws Exception {
        String[] invalidServiceNameCommands = {
            "|set service A", "|get service|A", "|get-public service:A", "|decrypt service|A YWJj", "|del service:A"
        };

        for (String command : invalidServiceNameCommands) {
            KeyValueStore store = mock(KeyValueStore.class);

            String message = runCommand(command, store);

            assertEquals(stringResponse(ProtocolError.INVALID_SERVICE_NAME), message);
            verify(store).ensureHealthy();
            verifyNoMoreInteractions(store);
        }
    }

    @Test
    void testMalformedDecryptCommandIsRejectedBeforeStoreAccess() throws Exception {
        KeyValueStore store = mock(KeyValueStore.class);

        String message = runCommand("|decrypt serviceA payload with spaces", store);

        assertEquals(stringResponse(ProtocolError.INVALID_INPUT_FORMAT), message);
        verify(store).ensureHealthy();
        verifyNoMoreInteractions(store);
    }

    @Test
    void testDecryptFailureDoesNotExposePrivateKeyInResponseOrLogs() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        String encodedPrivateKey = Base64.getEncoder()
                .encodeToString(generator.generateKeyPair().getPrivate().getEncoded());
        KeyValueStore store = mock(KeyValueStore.class);
        when(store.decrypt(org.mockito.ArgumentMatchers.eq("serviceA"), org.mockito.ArgumentMatchers.any(byte[].class)))
                .thenThrow(new GeneralSecurityException(encodedPrivateKey));

        Logger handlerLogger = (Logger) LoggerFactory.getLogger(ClientHandler.class);
        Logger auditLogger = (Logger) LoggerFactory.getLogger("io.canis.audit");
        ListAppender<ILoggingEvent> handlerAppender = new ListAppender<>();
        ListAppender<ILoggingEvent> auditAppender = new ListAppender<>();
        handlerAppender.start();
        auditAppender.start();
        handlerLogger.addAppender(handlerAppender);
        auditLogger.addAppender(auditAppender);
        try {
            String response = runCommand("|decrypt serviceA YWJj", store);
            assertEquals(stringResponse(ProtocolError.DECRYPTION_FAILED), response);
            assertFalse(response.contains(encodedPrivateKey));
            assertTrue(handlerAppender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .anyMatch(message -> message.contains("Failed to decrypt payload")));
            assertFalse(handlerAppender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .anyMatch(message -> message.contains(encodedPrivateKey)));
            assertFalse(auditAppender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .anyMatch(message -> message.contains(encodedPrivateKey)));
            assertTrue(handlerAppender.list.stream().allMatch(event -> event.getThrowableProxy() == null));
        } finally {
            handlerLogger.detachAppender(handlerAppender);
            auditLogger.detachAppender(auditAppender);
            handlerAppender.stop();
            auditAppender.stop();
        }
    }

    @Test
    void testInvalidServiceNameAuditLogDoesNotIncludeRawPayload() throws Exception {
        Logger auditLogger = (Logger) LoggerFactory.getLogger("io.canis.audit");
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        auditLogger.addAppender(appender);

        try {
            KeyValueStore store = mock(KeyValueStore.class);

            String message = runCommand("|set service|A", store);

            assertEquals(stringResponse(ProtocolError.INVALID_SERVICE_NAME), message);
            verify(store).ensureHealthy();
            verifyNoMoreInteractions(store);
            assertTrue(appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .anyMatch(logMessage -> logMessage.contains("event=command_rejected")
                            && logMessage.contains("reason=invalid_service_name")));
            assertFalse(appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .anyMatch(logMessage -> logMessage.contains("|set service|A") || logMessage.contains("service|A")));
        } finally {
            auditLogger.detachAppender(appender);
            appender.stop();
        }
    }

    private String readResponse(byte[] bytes) throws Exception {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            int length = in.readInt();
            byte[] payload = new byte[length];
            in.readFully(payload);
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    private String storedPrivateKey(Path database, Path secret) throws Exception {
        byte[] serialized = new SymmetricGenerator(secret.toString()).decrypt(Files.readAllBytes(database));
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(serialized))) {
            in.skipNBytes("CANISDB1".length());
            assertEquals(1, in.readInt());
            readStoredString(in);
            readStoredString(in);
            readStoredString(in);
            return readStoredString(in);
        }
    }

    private String readStoredString(DataInputStream in) throws Exception {
        int length = in.readInt();
        return new String(in.readNBytes(length), StandardCharsets.UTF_8);
    }

    private String stringResponse(ProtocolError error) {
        return "|s>" + error.wireMessage();
    }

    private String runCommand(String command, KeyValueStore store) throws Exception {
        Socket socket = mock(Socket.class);
        when(socket.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 3307));

        ByteArrayOutputStream response = new ByteArrayOutputStream();
        ClientHandler handler = new ClientHandler(
                socket, new BufferedReader(new StringReader(command)), new DataOutputStream(response), store);

        handler.run();

        return readResponse(response.toByteArray());
    }
}
