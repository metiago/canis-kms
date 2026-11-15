package io.canis.jpaw.client;

import io.canis.jpaw.config.TlsCipherPolicy;
import io.canis.jpaw.config.TlsConfiguration;
import io.canis.jpaw.config.TlsContextFactory;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;

/**
 * SocketClient is responsible for low-level communication with the server. This class handles the socket connection,
 * data transmission, and deserialization of received data.
 */
final class SocketClient implements AutoCloseable {

    private static final int SOCKET_TIMEOUT_MS = 30_000;
    private static final int MAX_COMMAND_CHARS = 1_048_576;
    private static final int MAX_RESPONSE_BYTES = 1_048_576;

    private final String username;
    private final String password;
    private final SSLSocket socket;
    private final PrintWriter out;
    private final DataInputStream dataInputStream;

    public SocketClient(String address, int port, String username, String password, TlsConfiguration tls)
            throws IOException {
        this.username = username;
        this.password = password;
        Socket transport = new Socket();
        SSLSocket connected = null;
        try {
            SSLContext context = TlsContextFactory.create(tls);
            transport.connect(new InetSocketAddress(address, port), SOCKET_TIMEOUT_MS);
            connected = (SSLSocket) context.getSocketFactory().createSocket(transport, address, port, true);
            connected.setSoTimeout(SOCKET_TIMEOUT_MS);
            TlsCipherPolicy.configure(connected);
            SSLParameters parameters = connected.getSSLParameters();
            parameters.setEndpointIdentificationAlgorithm("HTTPS");
            connected.setSSLParameters(parameters);
            connected.startHandshake();
            this.socket = connected;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.auth();
        } catch (IOException | RuntimeException e) {
            if (connected != null) {
                try {
                    connected.close();
                } catch (IOException ignored) {
                }
            } else {
                try {
                    transport.close();
                } catch (IOException ignored) {
                }
            }
            if (e instanceof IOException ioException) {
                throw ioException;
            }
            if (e instanceof TlsCipherPolicy.NoApprovedCipherSuiteException) {
                throw new IOException("No approved TLS 1.3 cipher suite is enabled.");
            }
            throw new IOException("TLS connection could not be established.");
        }
    }

    private void auth() throws IOException {
        try {

            String command = String.format("|login %s:%s", this.username, this.password);
            if (command.length() > MAX_COMMAND_CHARS) {
                throw new IOException("Command exceeds maximum length.");
            }
            out.println(command);

            String response = readResponse();
            if (!"Authentication successful".equals(response)) {
                throw new IOException("Authentication failed: " + response);
            }
        } catch (IOException e) {
            closeQuietly();
            throw e;
        }
    }

    /**
     * Sends a command to the server and retrieves the response as a string.
     *
     * @param command the command to send to the server
     * @return the response from the server as a String
     * @throws IOException if an error occurs during communication
     */
    public synchronized String sendCommand(String command) throws IOException {
        if (command.length() > MAX_COMMAND_CHARS) {
            throw new IOException("Command exceeds maximum length.");
        }

        out.println(command);
        if (out.checkError()) {
            throw new IOException("Failed to send command to server.");
        }

        return readResponse();
    }

    private String readResponse() throws IOException {
        int length = dataInputStream.readInt();
        if (length <= 0 || length > MAX_RESPONSE_BYTES) {
            throw new IOException("Invalid response from server.");
        }

        byte[] bytes = new byte[length];
        dataInputStream.readFully(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    private void closeQuietly() {
        try {
            close();
        } catch (IOException ignored) {
        }
    }
}
