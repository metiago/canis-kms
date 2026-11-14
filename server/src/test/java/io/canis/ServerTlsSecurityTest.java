package io.canis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.canis.config.TlsConfiguration;
import io.canis.config.TlsContextFactory;
import io.canis.store.KeyValueStore;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.net.ssl.SSLSocket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServerTlsSecurityTest {
  @TempDir Path tempDir;
  private TlsTestSupport tls;
  private Server server;
  private Thread serverThread;

  @BeforeEach
  void start() throws Exception {
    tls = new TlsTestSupport(tempDir);
    server = new Server(tls.environment(), new KeyValueStore(
        tempDir.resolve("db.dat").toString(), tempDir.resolve("secret.key").toString()));
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
      // JSSE sends a TLS alert record (content type 21), never a CANISP response.
      assertEquals(21, socket.getInputStream().read());
    }
  }

  @Test
  void rejectsTls12() throws Exception {
    try (SSLSocket socket = (SSLSocket) tls.clientContext().getSocketFactory()
        .createSocket("localhost", server.getPort())) {
      socket.setSoTimeout(3000);
      socket.setEnabledProtocols(new String[] {"TLSv1.2"});
      assertThrows(IOException.class, socket::startHandshake);
    }
  }

  @Test
  void rejectsUntrustedClientCertificate() throws Exception {
    try (SSLSocket socket = (SSLSocket) tls.untrustedClientContext().getSocketFactory()
        .createSocket("localhost", server.getPort())) {
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
  void invalidTlsMaterialPreventsStartup() {
    TlsConfiguration config = tls.environment().tls();
    assertThrows(IllegalStateException.class, () -> TlsContextFactory.create(
        new TlsConfiguration(tempDir.resolve("missing.p12"), config.keyStorePassword(),
            config.trustStore(), config.trustStorePassword())));
    assertThrows(IllegalStateException.class, () -> TlsContextFactory.create(
        new TlsConfiguration(config.keyStore(), "wrong-password",
            config.trustStore(), config.trustStorePassword())));
  }

  @Test
  void databaseTamperingStillFailsClosed() throws Exception {
    Path database = tempDir.resolve("db.dat");
    Path key = tempDir.resolve("secret.key");
    KeyValueStore store = new KeyValueStore(database.toString(), key.toString());
    store.set("sample", new io.canis.store.Entry());
    byte[] bytes = Files.readAllBytes(database);
    bytes[bytes.length - 1] ^= 1;
    Files.write(database, bytes);
    assertThrows(IllegalStateException.class,
        () -> new KeyValueStore(database.toString(), key.toString()));
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
