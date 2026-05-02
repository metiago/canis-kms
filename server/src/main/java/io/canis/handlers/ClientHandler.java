package io.canis.handlers;

import static io.canis.protocol.Commands.ADD;
import static io.canis.protocol.Commands.CANISP_VERSION;
import static io.canis.protocol.Commands.DECRYPT;
import static io.canis.protocol.Commands.DELETE;
import static io.canis.protocol.Commands.GET;
import static io.canis.protocol.Commands.GET_PUBLIC;
import static io.canis.protocol.Commands.HEALTH;
import static io.canis.protocol.Commands.INVALID_COMMAND;
import static io.canis.protocol.Commands.LIST;
import static io.canis.protocol.Commands.OK_COMMAND;
import static io.canis.protocol.Commands.VERSION;
import static io.canis.protocol.ProtocolError.DECRYPTION_FAILED;
import static io.canis.protocol.ProtocolError.INVALID_INPUT_FORMAT;
import static io.canis.protocol.ProtocolError.INVALID_SERVICE_NAME;
import static io.canis.protocol.ProtocolError.KEY_NOT_FOUND;
import static io.canis.protocol.ProtocolError.REQUEST_TOO_LARGE;

import io.canis.audit.AuditLogger;
import io.canis.crypto.AsymmetricGenerator;
import io.canis.crypto.Cryptographer;
import io.canis.net.BoundedLineReader;
import io.canis.net.BoundedLineReader.LineTooLongException;
import io.canis.protocol.Converter;
import io.canis.protocol.ServiceNameValidator;
import io.canis.store.Entry;
import io.canis.store.KeyValueStore;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

  private final Socket socket;
  private final BufferedReader in;
  private final DataOutputStream out;
  private final KeyValueStore store;
  private final String serviceIdentity;

  public ClientHandler(Socket socket, BufferedReader in, DataOutputStream out) {
    this(socket, in, out, new KeyValueStore());
  }

  public ClientHandler(Socket socket, BufferedReader in, DataOutputStream out, KeyValueStore store) {
    this(socket, in, out, store, null);
  }

  public ClientHandler(
      Socket socket,
      BufferedReader in,
      DataOutputStream out,
      KeyValueStore store,
      String serviceIdentity) {
    this.socket = socket;
    this.in = in;
    this.out = out;
    this.store = store;
    this.serviceIdentity = serviceIdentity;
  }

  public void run() {

    try {
      String input;
      while ((input = readCommand()) != null) {
        executeCommand(input, out);
      }

    } catch (IOException | NoSuchAlgorithmException e) {
      logger.error(e.getMessage(), e);
    } finally {
      try {
        socket.close();
      } catch (IOException e) {
        logger.warn(e.getMessage(), e);
      }
    }
  }

  private String readCommand() throws IOException {
    try {
      return BoundedLineReader.readLine(in);
    } catch (LineTooLongException e) {
      logger.warn("Request exceeded maximum size for IP {}", this.socket.getRemoteSocketAddress());
      AuditLogger.commandRejected(serviceIdentity, this.socket.getRemoteSocketAddress(), "request_too_large");
      sendResponse(out, REQUEST_TOO_LARGE.wireMessage());
      return null;
    }
  }

  private void executeCommand(String input, DataOutputStream out)
      throws IOException, NoSuchAlgorithmException {

    if (input.equals(VERSION)) {
      logger.info("Returning CANISP version for IP {}", this.socket.getRemoteSocketAddress());
      sendResponse(out, CANISP_VERSION);

    } else if (input.equals(HEALTH)) {
      logger.info("Performing health check for IP {}", this.socket.getRemoteSocketAddress());
      sendResponse(out, OK_COMMAND);

    } else if (input.startsWith(ADD)) {
      logger.info("Adding key for IP {}", this.socket.getRemoteSocketAddress());
      String serviceName = input.substring(ADD.length()).trim();
      if (!validateServiceName(serviceName, out)) {
        return;
      }
      add(serviceName);
      AuditLogger.keyCreated(serviceIdentity, serviceName, this.socket.getRemoteSocketAddress());
      sendResponse(out, OK_COMMAND);

    } else if (input.startsWith(GET_PUBLIC)) {
      logger.info("Getting public key for IP {}", this.socket.getRemoteSocketAddress());
      String serviceName = input.substring(GET_PUBLIC.length()).trim();
      if (!validateServiceName(serviceName, out)) {
        return;
      }
      AuditLogger.keyAccessed(serviceIdentity, serviceName, this.socket.getRemoteSocketAddress());
      sendResponse(out, getPublicKey(serviceName));

    } else if (input.startsWith(GET)) {
      logger.info("Getting key for IP {}", this.socket.getRemoteSocketAddress());
      String serviceName = input.substring(GET.length()).trim();
      if (!validateServiceName(serviceName, out)) {
        return;
      }
      Entry entry = get(serviceName);
      AuditLogger.keyAccessed(serviceIdentity, serviceName, this.socket.getRemoteSocketAddress());
      var bytes = Converter.toMap(entry.toMap()).getBytes(StandardCharsets.UTF_8);
      out.writeInt(bytes.length);
      out.write(bytes);

    } else if (input.startsWith(DECRYPT)) {
      logger.info("Decrypting payload for IP {}", this.socket.getRemoteSocketAddress());
      decrypt(input.substring(DECRYPT.length()).trim(), out);

    } else if (input.equals(LIST)) {
      logger.info("Listing keys for IP {}", this.socket.getRemoteSocketAddress());
      List<Entry> entries = list();
      AuditLogger.keyListed(serviceIdentity, this.socket.getRemoteSocketAddress());
      var bytes = Converter.toArrayOfMaps(entries).getBytes(StandardCharsets.UTF_8);
      out.writeInt(bytes.length);
      out.write(bytes);

    } else if (input.startsWith(DELETE)) {
      logger.info("Deleting key for IP {}", this.socket.getRemoteSocketAddress());
      String serviceName = input.substring(DELETE.length()).trim();
      if (!validateServiceName(serviceName, out)) {
        return;
      }
      delete(serviceName);
      AuditLogger.keyDeleted(serviceIdentity, serviceName, this.socket.getRemoteSocketAddress());
      sendResponse(out, OK_COMMAND);

    } else {
      logger.warn("Invalid command received from IP {}", this.socket.getRemoteSocketAddress());
      AuditLogger.commandRejected(serviceIdentity, this.socket.getRemoteSocketAddress(), "unknown_command");
      var resp = INVALID_COMMAND.getBytes(StandardCharsets.UTF_8);
      out.writeInt(resp.length);
      out.write(resp);
    }
  }

  private void sendResponse(DataOutputStream out, String command) throws IOException {
    var resp = String.format("%s%s", "|s>", command).getBytes(StandardCharsets.UTF_8);
    out.writeInt(resp.length);
    out.write(resp);
  }

  private boolean validateServiceName(String serviceName, DataOutputStream out) throws IOException {
    if (ServiceNameValidator.isValid(serviceName)) {
      return true;
    }

    logger.warn("Invalid service name received from IP {}", this.socket.getRemoteSocketAddress());
    AuditLogger.commandRejected(serviceIdentity, this.socket.getRemoteSocketAddress(), "invalid_service_name");
    sendResponse(out, INVALID_SERVICE_NAME.wireMessage());
    return false;
  }

  private List<Entry> list() throws IOException {
    return store.list();
  }

  private void add(String args) throws NoSuchAlgorithmException {
    logger.info("Adding new key with args: {}", args);
    KeyPair keyPair = AsymmetricGenerator.generateKeyPair();
    PublicKey publicKey = keyPair.getPublic();
    PrivateKey privateKey = keyPair.getPrivate();
    var publicKeyBase64 = AsymmetricGenerator.publicKeyToString(publicKey);
    var privateKeyBase64 = AsymmetricGenerator.privateKeyToString(privateKey);

    var metadata = new Entry();
    metadata.setName(args);
    metadata.setPublicKey(publicKeyBase64);
    metadata.setPrivateKey(privateKeyBase64);
    store.set(args, metadata);
  }

  private Entry get(String key) {
    logger.info("Getting by key: {}", key);
    return Optional.ofNullable(store.get(key)).orElse(new Entry());
  }

  private String getPublicKey(String key) {
    logger.info("Getting public key by key: {}", key);
    Entry entry = store.get(key);
    if (entry == null || entry.getPublicKey() == null) {
      return KEY_NOT_FOUND.wireMessage();
    }
    return entry.getPublicKey();
  }

  private void delete(String key) {
    logger.info("Deleting by key: {}", key);
    store.delete(key);
  }

  private void decrypt(String args, DataOutputStream out) throws IOException {
    String[] parts = args.split("\\s+");
    if (parts.length != 2) {
      AuditLogger.decryptFailed(
          serviceIdentity, "unknown", this.socket.getRemoteSocketAddress(), "invalid_input_format");
      sendResponse(out, INVALID_INPUT_FORMAT.wireMessage());
      return;
    }

    String key = parts[0];
    if (!validateServiceName(key, out)) {
      return;
    }

    logger.info("Service {} requested decrypt with key {}", serviceIdentity, key);

    Entry entry = store.get(key);
    if (entry == null || entry.getPrivateKey() == null) {
      AuditLogger.decryptFailed(serviceIdentity, key, this.socket.getRemoteSocketAddress(), "key_not_found");
      sendResponse(out, KEY_NOT_FOUND.wireMessage());
      return;
    }

    try {
      byte[] encryptedData = Base64.getDecoder().decode(parts[1]);
      PrivateKey privateKey = AsymmetricGenerator.stringToPrivateKey(entry.getPrivateKey());
      byte[] decryptedData = Cryptographer.decrypt(encryptedData, privateKey);
      AuditLogger.decryptSucceeded(serviceIdentity, key, this.socket.getRemoteSocketAddress());
      sendResponse(out, Base64.getEncoder().encodeToString(decryptedData));
    } catch (Exception e) {
      logger.error("Failed to decrypt payload for key {}", key, e);
      AuditLogger.decryptFailed(serviceIdentity, key, this.socket.getRemoteSocketAddress(), "decryption_failed");
      sendResponse(out, DECRYPTION_FAILED.wireMessage());
    }
  }
}
