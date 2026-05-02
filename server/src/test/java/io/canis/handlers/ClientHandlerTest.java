package io.canis.handlers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.canis.store.Entry;
import io.canis.store.KeyValueStore;
import io.canis.crypto.AsymmetricGenerator;
import io.canis.crypto.Cryptographer;
import io.canis.net.BoundedLineReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

public class ClientHandlerTest {

  @Test
  void testVersionCommandReturnsCanispVersion() throws Exception {
    KeyValueStore store = mock(KeyValueStore.class);

    Socket socket = mock(Socket.class);
    when(socket.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 3307));

    ByteArrayOutputStream response = new ByteArrayOutputStream();
    ClientHandler handler = new ClientHandler(
        socket,
        new BufferedReader(new StringReader("|version")),
        new DataOutputStream(response),
        store);

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
        socket,
        new BufferedReader(new StringReader(oversizedCommand)),
        new DataOutputStream(response),
        store);

    handler.run();

    assertEquals("|s>ERROR: Request too large", readResponse(response.toByteArray()));
  }

  @Test
  void testDecryptCommandUsesStoredPrivateKey() throws Exception {
    KeyPair keyPair = AsymmetricGenerator.generateKeyPair();
    String privateKey = AsymmetricGenerator.privateKeyToString(keyPair.getPrivate());
    String publicKey = AsymmetricGenerator.publicKeyToString(keyPair.getPublic());
    Entry entry = new Entry("serviceA", publicKey, privateKey);

    byte[] plaintext = "file-key".getBytes(StandardCharsets.UTF_8);
    byte[] encrypted = Cryptographer.encrypt(plaintext, keyPair.getPublic());

    KeyValueStore store = mock(KeyValueStore.class);
    when(store.get("serviceA")).thenReturn(entry);

    Socket socket = mock(Socket.class);
    when(socket.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 3307));

    String command = "|decrypt serviceA " + Base64.getEncoder().encodeToString(encrypted);
    ByteArrayOutputStream response = new ByteArrayOutputStream();
    ClientHandler handler = new ClientHandler(
        socket,
        new BufferedReader(new StringReader(command)),
        new DataOutputStream(response),
        store,
        "serviceB");

    handler.run();

    String message = readResponse(response.toByteArray());
    String decryptedBase64 = message.replace("|s>", "");

    assertArrayEquals(plaintext, Base64.getDecoder().decode(decryptedBase64));
    assertFalse(message.contains(privateKey));
  }

  @Test
  void testListCommandUsesStoredEntries() throws Exception {
    KeyValueStore store = mock(KeyValueStore.class);
    when(store.list()).thenReturn(List.of(
        new Entry("serviceA", "public-key-a", "private-key-a"),
        new Entry("serviceB", "public-key-b", "private-key-b")));

    Socket socket = mock(Socket.class);
    when(socket.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 3307));

    ByteArrayOutputStream response = new ByteArrayOutputStream();
    ClientHandler handler = new ClientHandler(
        socket,
        new BufferedReader(new StringReader("|list")),
        new DataOutputStream(response),
        store);

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
    when(store.get("serviceA")).thenReturn(
        new Entry("serviceA", "public-key-a", "private-key-a"));

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
  void testInvalidServiceNamesAreRejectedBeforeStoreAccess() throws Exception {
    String[] invalidServiceNameCommands = {
        "|set service A",
        "|get service|A",
        "|get-public service:A",
        "|decrypt service|A YWJj",
        "|del service:A"
    };

    for (String command : invalidServiceNameCommands) {
      KeyValueStore store = mock(KeyValueStore.class);

      String message = runCommand(command, store);

      assertEquals("|s>ERROR: Invalid service name", message);
      verifyNoInteractions(store);
    }
  }

  @Test
  void testMalformedDecryptCommandIsRejectedBeforeStoreAccess() throws Exception {
    KeyValueStore store = mock(KeyValueStore.class);

    String message = runCommand("|decrypt serviceA payload with spaces", store);

    assertEquals("|s>ERROR: Invalid input format", message);
    verifyNoInteractions(store);
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

      assertEquals("|s>ERROR: Invalid service name", message);
      verifyNoInteractions(store);
      assertTrue(appender.list.stream()
          .map(ILoggingEvent::getFormattedMessage)
          .anyMatch(logMessage -> logMessage.contains("event=command_rejected")
              && logMessage.contains("reason=invalid_service_name")));
      assertFalse(appender.list.stream()
          .map(ILoggingEvent::getFormattedMessage)
          .anyMatch(logMessage -> logMessage.contains("|set service|A")
              || logMessage.contains("service|A")));
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

  private String runCommand(String command, KeyValueStore store) throws Exception {
    Socket socket = mock(Socket.class);
    when(socket.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 3307));

    ByteArrayOutputStream response = new ByteArrayOutputStream();
    ClientHandler handler = new ClientHandler(
        socket,
        new BufferedReader(new StringReader(command)),
        new DataOutputStream(response),
        store);

    handler.run();

    return readResponse(response.toByteArray());
  }
}
