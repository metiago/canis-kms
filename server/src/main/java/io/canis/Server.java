package io.canis;

import io.canis.audit.AuditLogger;
import io.canis.config.Environment;
import io.canis.config.TlsCipherPolicy;
import io.canis.config.TlsContextFactory;
import io.canis.handlers.ClientHandler;
import io.canis.models.LoginCredentials;
import io.canis.net.BoundedLineReader;
import io.canis.net.BoundedLineReader.LineTooLongException;
import io.canis.store.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.canis.config.EnvironmentLoader.loadEnvironment;
import static io.canis.handlers.Command.LOGIN;

public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int SOCKET_TIMEOUT_MS = 30_000;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;

    private final ExecutorService executorService;

    private final int port;
    private final Map<String, String> serviceCredentials;
    private final KeyValueStore store;
    private final Path secretKeyPath;
    private final SSLContext tlsContext;
    private volatile boolean running;
    private volatile ServerSocket serverSocket;

    public Server() {
        this(loadEnvironment(), Path.of("db.dat"));
    }

    Server(Environment env, Path databasePath) {
        this(env, new KeyValueStore(databasePath.toString(), env.secretKeyPath().toString()));
    }

    public Server(Environment env, KeyValueStore store) {
        if (env == null || store == null) {
            throw new IllegalStateException("CANIS server configuration incomplete");
        }
        store.verifyConfiguration(env.secretKeyPath());
        this.secretKeyPath = env.secretKeyPath();
        this.port = env.port();
        this.serviceCredentials = env.serviceCredentials();
        this.store = store;
        this.tlsContext = TlsContextFactory.create(env.tls());
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void start() {
        try {
            store.verifyConfiguration(secretKeyPath);
            store.ensureHealthy();
        } catch (RuntimeException e) {
            shutdownExecutor();
            throw e;
        }
        running = true;
        try (SSLServerSocket boundServerSocket =
                (SSLServerSocket) tlsContext.getServerSocketFactory().createServerSocket()) {
            TlsCipherPolicy.configure(boundServerSocket);
            boundServerSocket.setNeedClientAuth(true);
            boundServerSocket.bind(new InetSocketAddress(this.port));
            this.serverSocket = boundServerSocket;
            logger.info("TLS server is listening on port {}", boundServerSocket.getLocalPort());
            while (running) {
                try {
                    SSLSocket socket = (SSLSocket) boundServerSocket.accept();
                    executorService.submit(() -> handleConnection(socket));
                } catch (SocketException e) {
                    if (running) {
                        logger.error("TLS server accept failed");
                        throw new IllegalStateException("TLS server accept failed");
                    }
                    break;
                }
            }

        } catch (TlsCipherPolicy.NoApprovedCipherSuiteException e) {
            logger.error("TLS server configuration rejected: no approved TLS 1.3 cipher suite is enabled");
            throw e;
        } catch (IOException e) {
            if (running) {
                logger.error("TLS server failed to bind or accept connections");
                throw new IllegalStateException("TLS server failed to start");
            }
        } finally {
            running = false;
            serverSocket = null;
            shutdownExecutor();
        }
    }

    public void stop() {
        running = false;
        ServerSocket currentServerSocket = serverSocket;
        if (currentServerSocket != null) {
            try {
                currentServerSocket.close();
            } catch (IOException e) {
                logger.warn("TLS server socket close failed");
            }
        }
    }

    public int getPort() {
        ServerSocket currentServerSocket = serverSocket;
        if (currentServerSocket == null) {
            return port;
        }
        return currentServerSocket.getLocalPort();
    }

    private void handleConnection(SSLSocket socket) {
        try {
            store.ensureHealthy();
            socket.setSoTimeout(SOCKET_TIMEOUT_MS);
            TlsCipherPolicy.configure(socket);
            socket.setNeedClientAuth(true);
            socket.startHandshake();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            Optional<String> serviceIdentity = authenticate(socket, in, out);
            if (serviceIdentity.isPresent()) {
                new ClientHandler(socket, in, out, store, serviceIdentity.get()).run();
            } else {
                socket.close();
            }
        } catch (SSLException e) {
            logger.warn("TLS handshake or connection failed for {}", socket.getRemoteSocketAddress());
            closeQuietly(socket);
        } catch (IOException e) {
            logger.warn("Client connection failed for {}", socket.getRemoteSocketAddress());
            closeQuietly(socket);
        } catch (TlsCipherPolicy.NoApprovedCipherSuiteException e) {
            logger.warn("TLS connection rejected: no approved TLS 1.3 cipher suite is enabled");
            closeQuietly(socket);
        } catch (IllegalStateException e) {
            logger.error("CANIS database unavailable; stopping TLS listener");
            closeQuietly(socket);
            stop();
        } finally {
            if (!store.isHealthy()) {
                stop();
            }
        }
    }

    private Optional<String> authenticate(Socket socket, BufferedReader in, DataOutputStream out) throws IOException {

        String input;
        try {
            input = BoundedLineReader.readLine(in);
        } catch (LineTooLongException e) {
            logger.warn("Authentication request exceeded maximum size: {}", socket.getRemoteSocketAddress());
            AuditLogger.authenticationFailed("unknown", socket.getRemoteSocketAddress(), "request_too_large");
            sendResponse(out, "Request too large");
            return Optional.empty();
        }

        if (input == null || !isLoginCommand(input)) {
            logger.info("Authentication failed: missing or invalid login command");
            AuditLogger.authenticationFailed("unknown", socket.getRemoteSocketAddress(), "missing_login");
            sendResponse(out, "Authentication failed");
            return Optional.empty();
        }

        Optional<LoginCredentials> credentials = parseLoginCredentials(input);
        if (credentials.isEmpty()) {
            logger.info("Invalid input format when authenticating: {}", socket.getRemoteSocketAddress());
            AuditLogger.authenticationFailed("unknown", socket.getRemoteSocketAddress(), "invalid_login_format");
            sendResponse(out, "Invalid input format");
            return Optional.empty();
        }

        String username = credentials.get().username();
        String password = credentials.get().password();

        if (isCredentialValid(username, password)) {
            logger.info("Authentication successful for username: {}", username);
            AuditLogger.authenticationSucceeded(username, socket.getRemoteSocketAddress());
            sendResponse(out, "Authentication successful");
            return Optional.of(username);
        }

        logger.info("Authentication failed: invalid credentials");
        AuditLogger.authenticationFailed("unknown", socket.getRemoteSocketAddress(), "invalid_credentials");
        sendResponse(out, "Authentication failed");
        return Optional.empty();
    }

    private void sendResponse(DataOutputStream out, String response) throws IOException {
        var resp = response.getBytes(StandardCharsets.UTF_8);
        out.writeInt(resp.length);
        out.write(resp);
    }

    private boolean isCredentialValid(String u, String p) {
        return p.equals(this.serviceCredentials.get(u));
    }

    static Optional<LoginCredentials> parseLoginCredentials(String input) {
        if (!isLoginCommand(input)) {
            return Optional.empty();
        }

        String args = input.substring(LOGIN.length()).trim();
        int separator = args.indexOf(':');
        if (separator < 0) {
            return Optional.empty();
        }

        String username = args.substring(0, separator).trim();
        String password = args.substring(separator + 1).trim();
        if (username.isEmpty() || password.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new LoginCredentials(username, password));
    }

    private static boolean isLoginCommand(String input) {
        return input != null && (input.equals(LOGIN) || input.startsWith(LOGIN + " "));
    }

    private void closeQuietly(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            logger.warn("Client socket close failed");
        }
    }

    private void shutdownExecutor() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }

    public static void main(String[] args) {
        if (args.length == 1 && "--init-db".equals(args[0])) {
            Environment env = loadEnvironment();
            KeyValueStore.initialize("db.dat", env.secretKeyPath().toString());
            return;
        }
        if (args.length != 0) {
            throw new IllegalArgumentException("Usage: Server [--init-db]");
        }
        new Server().start();
    }
}
