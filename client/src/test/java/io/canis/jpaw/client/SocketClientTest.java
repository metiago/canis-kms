package io.canis.jpaw.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.canis.jpaw.config.TlsConfiguration;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.net.ssl.SSLSocket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.io.TempDir;

class SocketClientTest {
    private static final int TOO_LARGE_RESPONSE_BYTES = 1_048_577;

    @TempDir
    Path tempDir;

    private ClientTlsTestSupport tls;

    @BeforeEach
    void setUp() throws Exception {
        tls = new ClientTlsTestSupport(tempDir);
    }

    @Test
    void connectsWithTls13AndKeepsFramingLimits() throws Exception {
        try (var server = tls.server(true)) {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Thread thread = new Thread(() -> authenticateAndWaitForClose(server, failure));
            thread.start();
            try (SocketClient client =
                    new SocketClient("localhost", server.getLocalPort(), "admin", "123", tls.clientConfiguration())) {
                assertThrows(IOException.class, () -> client.sendCommand("x".repeat(1_048_577)));
            }
            thread.join(3000);
            if (failure.get() != null) {
                throw new AssertionError("TLS test server failed", failure.get());
            }
        }
    }

    @Test
    void rejectsOversizedTlsResponseBeforeAllocation() throws Exception {
        try (var server = tls.server(true)) {
            Thread thread = new Thread(() -> sendOversizedAuthResponse(server));
            thread.start();
            assertThrows(
                    IOException.class,
                    () -> new SocketClient(
                            "localhost", server.getLocalPort(), "admin", "123", tls.clientConfiguration()));
            thread.join(3000);
        }
    }

    @Test
    void officialClientUsesTlsForAuthenticatedCommands() throws Exception {
        try (var server = tls.server(true)) {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Thread thread = new Thread(() -> serveCanisp(server, failure));
            thread.start();
            var client = new JpawClient(
                    new SocketClient("localhost", server.getLocalPort(), "admin", "123", tls.clientConfiguration()));
            try {
                assertEquals("OK", client.health());
            } finally {
                client.close();
            }
            thread.join(3000);
            if (failure.get() != null) {
                throw new AssertionError("TLS CANISP test server failed", failure.get());
            }
        }
    }

    @Test
    void officialClientCompletesFileEnvelopeWorkflowOverMutualTls() throws Exception {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(2048);
        KeyPair wrappingKey = keyGenerator.generateKeyPair();
        try (var server = tls.server(true)) {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Thread thread = new Thread(() -> serveFileWorkflow(server, wrappingKey, failure));
            thread.start();
            Path plain = tempDir.resolve("plain.txt");
            Path encrypted = tempDir.resolve("plain.canis");
            Path decrypted = tempDir.resolve("plain.dec.txt");
            String content = "TLS protected CANIS file flow\n".repeat(50);
            Files.writeString(plain, content, StandardCharsets.UTF_8);
            try (var client = new JpawClient(
                    new SocketClient("localhost", server.getLocalPort(), "admin", "123", tls.clientConfiguration()))) {
                client.encryptFile("serviceA", plain.toFile(), encrypted.toFile());
                client.decryptFile("serviceA", encrypted.toFile(), decrypted.toFile());
            }
            thread.join(3000);
            if (failure.get() != null) {
                throw new AssertionError("TLS file workflow server failed", failure.get());
            }
            assertEquals(content, Files.readString(decrypted, StandardCharsets.UTF_8));
        }
    }

    @Test
    void rejectsUntrustedServerBeforeSendingLogin() throws Exception {
        try (var server = tls.server(true)) {
            AtomicReference<String> received = new AtomicReference<>();
            Thread thread = new Thread(() -> acceptAndRead(server, received));
            thread.start();
            assertThrows(
                    IOException.class,
                    () -> new SocketClient(
                            "localhost", server.getLocalPort(), "admin", "123", tls.wrongHostTrustConfiguration()));
            thread.join(3000);
            assertNull(received.get());
        }
    }

    @Test
    void authenticatesWithValidCaSignedServerCertificate() throws Exception {
        try (var server = tls.serverWithValidCaCertificate()) {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Thread thread = new Thread(() -> authenticateAndWaitForClose(server, failure));
            thread.start();
            try (SocketClient ignored = new SocketClient(
                    "localhost", server.getLocalPort(), "admin", "123", tls.certificateAuthorityTrustConfiguration())) {
                // Construction completes the TLS handshake and CANISP login.
            }
            thread.join(3000);
            assertFalse(thread.isAlive());
            if (failure.get() != null) {
                throw new AssertionError("TLS test server failed", failure.get());
            }
        }
    }

    @Test
    void rejectsExpiredServerCertificateBeforeSendingLogin() throws Exception {
        try (var server = tls.serverWithExpiredCertificate()) {
            AtomicReference<String> received = new AtomicReference<>();
            Thread thread = new Thread(() -> acceptAndRead(server, received));
            thread.start();
            assertThrows(
                    IOException.class,
                    () -> new SocketClient(
                            "localhost",
                            server.getLocalPort(),
                            "admin",
                            "123",
                            tls.certificateAuthorityTrustConfiguration()));
            thread.join(3000);
            assertFalse(thread.isAlive());
            assertNull(received.get());
        }
    }

    @Test
    void rejectsServerCertificateWithWrongExtendedKeyUsageBeforeSendingLogin() throws Exception {
        try (var server = tls.serverWithWrongUsageCertificate()) {
            AtomicReference<String> received = new AtomicReference<>();
            Thread thread = new Thread(() -> acceptAndRead(server, received));
            thread.start();
            assertThrows(
                    IOException.class,
                    () -> new SocketClient(
                            "localhost",
                            server.getLocalPort(),
                            "admin",
                            "123",
                            tls.certificateAuthorityTrustConfiguration()));
            thread.join(3000);
            assertFalse(thread.isAlive());
            assertNull(received.get());
        }
    }

    @Test
    void rejectsMalformedTrustedServerStoreBeforeConnecting() throws Exception {
        Path malformed = tempDir.resolve("malformed-trust.p12");
        Files.write(malformed, new byte[] {1, 2, 3, 4});
        TlsConfiguration valid = tls.clientConfiguration();
        TlsConfiguration invalid =
                new TlsConfiguration(valid.keyStore(), valid.keyStorePassword(), malformed, valid.trustStorePassword());

        IOException failure =
                assertThrows(IOException.class, () -> new SocketClient("localhost", 1, "admin", "123", invalid));
        assertEquals("TLS connection could not be established.", failure.getMessage());
    }

    @Test
    void rejectsHostnameMismatch() throws Exception {
        try (var server = tls.server(true)) {
            AtomicReference<String> received = new AtomicReference<>();
            Thread thread = new Thread(() -> acceptAndRead(server, received));
            thread.start();
            assertThrows(
                    IOException.class,
                    () -> new SocketClient(
                            "127.0.0.1", server.getLocalPort(), "admin", "123", tls.clientConfiguration()));
            thread.join(3000);
            assertNull(received.get());
        }
    }

    @Test
    void rejectsUntrustedClientCertificate() throws Exception {
        try (var server = tls.server(false)) {
            AtomicReference<String> received = new AtomicReference<>();
            Thread thread = new Thread(() -> acceptAndRead(server, received));
            thread.start();
            assertThrows(
                    IOException.class,
                    () -> new SocketClient(
                            "localhost", server.getLocalPort(), "admin", "123", tls.clientConfiguration()));
            thread.join(3000);
            assertNull(received.get());
        }
    }

    @Test
    void rejectsPlaintextEndpointWithoutFallback() throws Exception {
        try (var server = new java.net.ServerSocket(0)) {
            AtomicReference<Integer> firstByte = new AtomicReference<>();
            Thread thread = new Thread(() -> {
                try (var socket = server.accept()) {
                    socket.setSoTimeout(3000);
                    firstByte.set(socket.getInputStream().read());
                    socket.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
                } catch (IOException ignored) {
                }
            });
            thread.start();
            assertThrows(
                    IOException.class,
                    () -> new SocketClient(
                            "localhost", server.getLocalPort(), "admin", "123", tls.clientConfiguration()));
            thread.join(3000);
            assertFalse(thread.isAlive());
            assertEquals(22, firstByte.get());
        }
    }

    @Test
    void rejectsTls12OnlyPeer() throws Exception {
        try (var server = tls.server(true)) {
            server.setEnabledProtocols(new String[] {"TLSv1.2"});
            AtomicReference<String> received = new AtomicReference<>();
            Thread thread = new Thread(() -> acceptAndRead(server, received));
            thread.start();
            assertThrows(
                    IOException.class,
                    () -> new SocketClient(
                            "localhost", server.getLocalPort(), "admin", "123", tls.clientConfiguration()));
            thread.join(3000);
            assertNull(received.get());
        }
    }

    @Test
    void negotiatesEveryProviderEnabledApprovedTls13Suite(TestReporter reporter) throws Exception {
        String[] approved = {"TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384", "TLS_CHACHA20_POLY1305_SHA256"};
        int tested = 0;
        var providerDisabled = new java.util.ArrayList<String>();
        for (String suite : approved) {
            try (var server = tls.server(true)) {
                if (!java.util.Arrays.asList(server.getEnabledCipherSuites()).contains(suite)) {
                    providerDisabled.add(suite);
                    continue;
                }
                server.setEnabledCipherSuites(new String[] {suite});
                AtomicReference<String> negotiated = new AtomicReference<>();
                AtomicReference<Throwable> failure = new AtomicReference<>();
                Thread thread = new Thread(() -> {
                    try (SSLSocket socket = (SSLSocket) server.accept()) {
                        socket.setSoTimeout(3000);
                        socket.startHandshake();
                        negotiated.set(socket.getSession().getCipherSuite());
                        new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
                        writeResponse(new DataOutputStream(socket.getOutputStream()), "Authentication successful");
                    } catch (Throwable e) {
                        failure.set(e);
                    }
                });
                thread.start();
                try (SocketClient ignored = new SocketClient(
                        "localhost", server.getLocalPort(), "admin", "123", tls.clientConfiguration())) {
                    // Construction completes the authenticated TLS handshake.
                }
                thread.join(3000);
                if (failure.get() != null) {
                    throw new AssertionError("TLS test server failed", failure.get());
                }
                assertEquals(suite, negotiated.get());
                tested++;
            }
        }
        reporter.publishEntry(
                "providerDisabledSuites", providerDisabled.isEmpty() ? "none" : String.join(",", providerDisabled));
        assertTrue(tested > 0);
    }

    private void authenticateAndWaitForClose(javax.net.ssl.SSLServerSocket server, AtomicReference<Throwable> failure) {
        try (SSLSocket socket = (SSLSocket) server.accept()) {
            socket.setSoTimeout(3000);
            socket.startHandshake();
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var out = new DataOutputStream(socket.getOutputStream());
            in.readLine();
            byte[] response = "Authentication successful".getBytes(StandardCharsets.UTF_8);
            out.writeInt(response.length);
            out.write(response);
            out.flush();
            in.readLine();
        } catch (Throwable e) {
            failure.set(e);
        }
    }

    private void acceptAndRead(javax.net.ssl.SSLServerSocket server, AtomicReference<String> received) {
        try (SSLSocket socket = (SSLSocket) server.accept()) {
            socket.setSoTimeout(3000);
            socket.startHandshake();
            received.set(new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine());
        } catch (IOException ignored) {
        }
    }

    private void serveCanisp(javax.net.ssl.SSLServerSocket server, AtomicReference<Throwable> failure) {
        try (SSLSocket socket = (SSLSocket) server.accept()) {
            socket.setSoTimeout(3000);
            socket.startHandshake();
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var out = new DataOutputStream(socket.getOutputStream());
            in.readLine();
            writeResponse(out, "Authentication successful");
            assertEquals("|health", in.readLine());
            writeResponse(out, "|s>OK");
        } catch (Throwable e) {
            failure.set(e);
        }
    }

    private void sendOversizedAuthResponse(javax.net.ssl.SSLServerSocket server) {
        try (SSLSocket socket = (SSLSocket) server.accept()) {
            socket.setSoTimeout(3000);
            socket.startHandshake();
            new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
            var out = new DataOutputStream(socket.getOutputStream());
            out.writeInt(TOO_LARGE_RESPONSE_BYTES);
            out.flush();
        } catch (IOException ignored) {
        }
    }

    private void writeResponse(DataOutputStream out, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
    }

    private void serveFileWorkflow(
            javax.net.ssl.SSLServerSocket server, KeyPair wrappingKey, AtomicReference<Throwable> failure) {
        try (SSLSocket socket = (SSLSocket) server.accept()) {
            socket.setSoTimeout(5000);
            socket.startHandshake();
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var out = new DataOutputStream(socket.getOutputStream());
            in.readLine();
            writeResponse(out, "Authentication successful");
            String publicKey =
                    Base64.getEncoder().encodeToString(wrappingKey.getPublic().getEncoded());
            assertEquals("|get-public serviceA", in.readLine());
            writeResponse(out, "|s>" + publicKey);
            String decrypt = in.readLine();
            String encodedKey = decrypt.split("\\s+", 3)[2];
            byte[] wrappedKey = Base64.getDecoder().decode(encodedKey);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    wrappingKey.getPrivate(),
                    new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
            byte[] dataKey = cipher.doFinal(wrappedKey);
            writeResponse(out, "|s>" + Base64.getEncoder().encodeToString(dataKey));
        } catch (Throwable e) {
            failure.set(e);
        }
    }
}
