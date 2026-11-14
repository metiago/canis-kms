package io.canis.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.canis.Server;
import io.canis.TlsTestSupport;
import io.canis.store.KeyValueStore;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SocketTest {
    @TempDir
    static Path tempDir;

    private static TlsTestSupport tls;
    private static Server server;

    CountDownLatch latch = new CountDownLatch(3);
    Exception[] exceptions = new Exception[3];
    AssertionError[] assertionErrors = new AssertionError[3];

    @BeforeAll
    static void serverStart() throws Exception {
        tls = new TlsTestSupport(tempDir);
        server = new Server(
                tls.environment(),
                new KeyValueStore(
                        tempDir.resolve("db.dat").toString(),
                        tempDir.resolve("secret.key").toString()));
        Thread serverThread = new Thread(server::start);
        serverThread.setDaemon(true);
        serverThread.start();
        long deadline = System.currentTimeMillis() + 3000;
        while (server.getPort() == 0 && System.currentTimeMillis() < deadline) {
            TimeUnit.MILLISECONDS.sleep(10);
        }
    }

    @AfterAll
    static void serverStop() {
        server.stop();
    }

    @Test
    void testLoginOK() throws Exception {
        var resp = sendCommand(String.format("|login %s:%s", username(), password()));
        assertEquals("Authentication successful", resp);
    }

    @Test
    void testInvalidCommandError() throws Exception {
        var resp = sendCommand("|health");
        assertEquals("Authentication failed", resp);
    }

    @Test
    void testMalformedLoginError() throws Exception {
        var resp = sendCommand("|login");
        assertEquals("Invalid input format", resp);
    }

    @Test
    public void testMultipleThreadOK() throws Exception {

        var t1 = new Thread(() -> {
            try {
                String resp = sendAuthenticatedCommand("|health");
                assertEquals("|s>OK", resp);
            } catch (AssertionError e) {
                assertionErrors[0] = e;
            } catch (Exception e) {
                exceptions[0] = e;
            } finally {
                latch.countDown();
            }
        });

        var t2 = new Thread(() -> {
            try {
                String resp = sendAuthenticatedCommand(String.format("|set %s", "appKey"));
                assertEquals("|s>OK", resp);
            } catch (AssertionError e) {
                assertionErrors[1] = e;
            } catch (Exception e) {
                exceptions[1] = e;
            } finally {
                latch.countDown();
            }
        });

        var t3 = new Thread(() -> {
            try {
                String resp = sendAuthenticatedCommand(String.format("|set %s", "appKey2"));
                assertEquals("|s>OK", resp);
            } catch (AssertionError e) {
                assertionErrors[2] = e;
            } catch (Exception e) {
                exceptions[2] = e;
            } finally {
                latch.countDown();
            }
        });

        t1.start();
        t2.start();
        t3.start();

        latch.await();

        for (AssertionError error : assertionErrors) {
            if (error != null) {
                fail("Test failed: " + error.getMessage());
            }
        }

        for (Exception e : exceptions) {
            if (e != null) {
                fail("Test failed: " + e.getMessage());
            }
        }

        String resp = sendAuthenticatedCommand(String.format("|get %s", "appKey"));
        assertTrue(resp.matches("\\|ms>.*"));
    }

    String sendCommand(String command) throws Exception {

        try (Socket socket = tls.connect(server.getPort());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                InputStream inputStream = socket.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(inputStream)) {

            out.println(command);

            return readResponse(dataInputStream);
        }
    }

    String sendAuthenticatedCommand(String command) throws Exception {

        try (Socket socket = tls.connect(server.getPort());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                InputStream inputStream = socket.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(inputStream)) {

            out.println(String.format("|login %s:%s", username(), password()));
            assertEquals("Authentication successful", readResponse(dataInputStream));

            out.println(command);

            return readResponse(dataInputStream);
        }
    }

    String readResponse(DataInputStream dataInputStream) throws IOException {
        int length = dataInputStream.readInt();
        byte[] bytes = new byte[length];
        dataInputStream.readFully(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    String username() {
        return "admin";
    }

    String password() {
        return "123";
    }
}
