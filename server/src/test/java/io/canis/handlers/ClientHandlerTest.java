package io.canis.handlers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.canis.store.Entry;
import io.canis.store.KeyValueStore;
import io.canis.utils.AsymmetricGenerator;
import io.canis.utils.Cryptographer;
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

public class ClientHandlerTest {

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

  private String readResponse(byte[] bytes) throws Exception {
    try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
      int length = in.readInt();
      byte[] payload = new byte[length];
      in.readFully(payload);
      return new String(payload, StandardCharsets.UTF_8);
    }
  }
}
