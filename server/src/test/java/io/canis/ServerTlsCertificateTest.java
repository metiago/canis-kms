package io.canis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.canis.store.KeyValueStore;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServerTlsCertificateTest {
    @TempDir
    Path tempDir;

    @Test
    void acceptsValidCaSignedClientCertificate() throws Exception {
        TlsTestSupport tls = new TlsTestSupport(tempDir);
        try (RunningServer server = startServer(tls);
                SSLSocket socket = connect(tls.validCaSignedClientContext(), server.port())) {
            socket.startHandshake();
            new PrintWriter(socket.getOutputStream(), true).println(loginCommand(tls));
            assertEquals("Authentication successful", readResponse(socket));
        }
    }

    @Test
    void rejectsExpiredClientCertificateBeforeLogin() throws Exception {
        TlsTestSupport tls = new TlsTestSupport(tempDir);
        try (RunningServer server = startServer(tls)) {
            assertRejectedBeforeLogin(tls.expiredClientContext(), server.port(), loginCommand(tls));
        }
    }

    @Test
    void rejectsClientCertificateWithWrongExtendedKeyUsageBeforeLogin() throws Exception {
        TlsTestSupport tls = new TlsTestSupport(tempDir);
        try (RunningServer server = startServer(tls)) {
            assertRejectedBeforeLogin(tls.wrongUsageClientContext(), server.port(), loginCommand(tls));
        }
    }

    private RunningServer startServer(TlsTestSupport tls) throws Exception {
        MasterKeyTestSupport.provision(tempDir.resolve("secret.key"));
        Server server = new Server(
                tls.environmentTrustingCertificateAuthority(),
                new KeyValueStore(
                        tempDir.resolve("db.dat").toString(),
                        tempDir.resolve("secret.key").toString()));
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                server.start();
            } catch (Throwable e) {
                failure.set(e);
            }
        });
        thread.start();
        long deadline = System.currentTimeMillis() + 3000;
        while (server.getPort() == 0 && thread.isAlive() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertTrue(server.getPort() > 0, "TLS test server did not bind");
        return new RunningServer(server, thread, failure);
    }

    private SSLSocket connect(SSLContext context, int port) throws Exception {
        SSLSocket socket = (SSLSocket) context.getSocketFactory().createSocket("localhost", port);
        socket.setSoTimeout(3000);
        socket.setEnabledProtocols(new String[] {"TLSv1.3"});
        var parameters = socket.getSSLParameters();
        parameters.setEndpointIdentificationAlgorithm("HTTPS");
        socket.setSSLParameters(parameters);
        return socket;
    }

    private void assertRejectedBeforeLogin(SSLContext context, int port, String loginCommand) throws Exception {
        try (SSLSocket socket = connect(context, port)) {
            assertThrows(IOException.class, () -> {
                socket.startHandshake();
                new PrintWriter(socket.getOutputStream(), true).println(loginCommand);
                readResponse(socket);
            });
        }
    }

    private String loginCommand(TlsTestSupport tls) {
        return "|login " + tls.environment().username() + ":"
                + tls.environment().password();
    }

    private String readResponse(SSLSocket socket) throws IOException {
        DataInputStream input = new DataInputStream(socket.getInputStream());
        int length = input.readInt();
        if (length <= 0 || length > 1024) {
            throw new IOException("Invalid test response length");
        }
        byte[] bytes = new byte[length];
        input.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private record RunningServer(Server server, Thread thread, AtomicReference<Throwable> failure)
            implements AutoCloseable {
        int port() {
            return server.getPort();
        }

        @Override
        public void close() throws InterruptedException {
            server.stop();
            thread.join(3000);
            assertFalse(thread.isAlive(), "TLS test server did not stop");
            assertNull(failure.get(), "TLS test server failed");
        }
    }
}
