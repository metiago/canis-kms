package io.canis.handlers;

import static io.canis.handlers.Commands.ADD;
import static io.canis.handlers.Commands.DECRYPT;
import static io.canis.handlers.Commands.DELETE;
import static io.canis.handlers.Commands.GET;
import static io.canis.handlers.Commands.HEALTH;
import static io.canis.handlers.Commands.INVALID_COMMAND;
import static io.canis.handlers.Commands.LIST;
import static io.canis.handlers.Commands.OK_COMMAND;

import io.canis.store.Entry;
import io.canis.store.KeyValueStore;
import io.canis.utils.AsymmetricGenerator;
import io.canis.utils.Converter;
import io.canis.utils.Cryptographer;
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
      while ((input = in.readLine()) != null) {
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

  private void executeCommand(String input, DataOutputStream out)
      throws IOException, NoSuchAlgorithmException {

    if (input.equals(HEALTH)) {
      logger.info("Performing health check for IP {}", this.socket.getRemoteSocketAddress());
      sendResponse(out, OK_COMMAND);

    } else if (input.startsWith(ADD)) {
      logger.info("Adding key for IP {}", this.socket.getRemoteSocketAddress());
      String args = input.substring(4).trim();
      add(args);
      sendResponse(out, OK_COMMAND);

    } else if (input.startsWith(GET)) {
      logger.info("Getting key for IP {}", this.socket.getRemoteSocketAddress());
      String args = input.substring(4).trim();
      Entry entry = get(args);
      var bytes = Converter.toMap(entry.toMap()).getBytes(StandardCharsets.UTF_8);
      out.writeInt(bytes.length);
      out.write(bytes);

    } else if (input.startsWith(DECRYPT)) {
      logger.info("Decrypting payload for IP {}", this.socket.getRemoteSocketAddress());
      decrypt(input.substring(DECRYPT.length()).trim(), out);

    } else if (input.equals(LIST)) {
      logger.info("Listing keys for IP {}", this.socket.getRemoteSocketAddress());
      List<Entry> entries = list();
      var bytes = Converter.toArrayOfMaps(entries).getBytes(StandardCharsets.UTF_8);
      out.writeInt(bytes.length);
      out.write(bytes);

    } else if (input.startsWith(DELETE)) {
      logger.info("Deleting key for IP {}", this.socket.getRemoteSocketAddress());
      String args = input.substring(4).trim();
      delete(args);
      sendResponse(out, OK_COMMAND);

    } else {
      logger.warn("Invalid command received from IP {}: {}", this.socket.getRemoteSocketAddress(), input);
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

  private void delete(String key) {
    logger.info("Deleting by key: {}", key);
    store.delete(key);
  }

  private void decrypt(String args, DataOutputStream out) throws IOException {
    String[] parts = args.split("\\s+", 2);
    if (parts.length < 2) {
      sendResponse(out, "ERROR: Invalid input format");
      return;
    }

    String key = parts[0];
    if (!isAuthorizedForPrivateKeyOperation(key)) {
      logger.warn("Service {} is not authorized to decrypt with key {}", serviceIdentity, key);
      sendResponse(out, "ERROR: Unauthorized");
      return;
    }

    Entry entry = store.get(key);
    if (entry == null || entry.getPrivateKey() == null) {
      sendResponse(out, "ERROR: Key not found");
      return;
    }

    try {
      byte[] encryptedData = Base64.getDecoder().decode(parts[1]);
      PrivateKey privateKey = AsymmetricGenerator.stringToPrivateKey(entry.getPrivateKey());
      byte[] decryptedData = Cryptographer.decrypt(encryptedData, privateKey);
      sendResponse(out, Base64.getEncoder().encodeToString(decryptedData));
    } catch (Exception e) {
      logger.error("Failed to decrypt payload for key {}", key, e);
      sendResponse(out, "ERROR: Decryption failed");
    }
  }

  private boolean isAuthorizedForPrivateKeyOperation(String key) {
    return serviceIdentity != null && serviceIdentity.equals(key);
  }
}
