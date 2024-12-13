package io.canis.handlers;

import static io.canis.handlers.Commands.ADD;
import static io.canis.handlers.Commands.DELETE;
import static io.canis.handlers.Commands.GET;
import static io.canis.handlers.Commands.HEALTH;
import static io.canis.handlers.Commands.INVALID_COMMAND;
import static io.canis.handlers.Commands.LIST;
import static io.canis.handlers.Commands.OK_COMMAND;

import io.canis.models.Entry;
import io.canis.store.KeyValueStore;
import io.canis.utils.AsymmetricPairGenerator;
import io.canis.utils.Converter;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

  private final Socket socket;

  public ClientHandler(Socket socket) {
    this.socket = socket;
  }

  public void run() {

    try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

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

      var resp = String.format("%s%s", "|s>", OK_COMMAND).getBytes(StandardCharsets.UTF_8);
      out.writeInt(resp.length);
      out.write(resp);

    } else if (input.startsWith(ADD)) {

      String args = input.substring(4).trim();
      add(args);

      var resp = String.format("%s%s", "|s>", OK_COMMAND).getBytes(StandardCharsets.UTF_8);
      out.writeInt(resp.length);
      out.write(resp);

    } else if (input.startsWith(GET)) {

      String args = input.substring(4).trim();
      Entry entry = get(args);

      var bytes = Converter.mapToString(entry.toMap()).getBytes(StandardCharsets.UTF_8);
      out.writeInt(bytes.length);
      out.write(bytes);

    } else if (input.equals(LIST)) {

      List<Entry> entries = list();
      var bytes = Converter.arrayOfMapsToString(entries).getBytes(StandardCharsets.UTF_8);
      out.writeInt(bytes.length);
      out.write(bytes);

    } else if (input.startsWith(DELETE)) {

      String args = input.substring(4).trim();

      delete(args);

      var resp = String.format("%s%s", "|s>", OK_COMMAND).getBytes(StandardCharsets.UTF_8);
      out.writeInt(resp.length);
      out.write(resp);

    } else {

      var resp = INVALID_COMMAND.getBytes(StandardCharsets.UTF_8);
      out.writeInt(resp.length);
      out.write(resp);
    }

  }

  private List<Entry> list() {
    return List.of(new Entry());
  }

  private void add(String args) throws NoSuchAlgorithmException, IOException {
    logger.info("Adding new secret: {}", args);
    KeyPair keyPair = AsymmetricPairGenerator.generateKeyPair();
    PublicKey publicKey = keyPair.getPublic();
    PrivateKey privateKey = keyPair.getPrivate();
    var publicKeyBase64 = AsymmetricPairGenerator.publicKeyToString(publicKey);
    var privateKeyBase64 = AsymmetricPairGenerator.privateKeyToString(privateKey);

    var store = new KeyValueStore();
    var metadata = new Entry();
    metadata.setName(args);
    metadata.setPublicKey(publicKeyBase64);
    metadata.setPrivateKey(privateKeyBase64);
    store.set(args, metadata);
  }

  private Entry get(String key) throws NoSuchAlgorithmException, IOException {
    logger.info("Getting by key: {}", key);
    return Optional.ofNullable(new KeyValueStore().get(key)).orElse(new Entry());
  }

  private void delete(String key) throws NoSuchAlgorithmException, IOException {
    logger.info("Deleting by key: {}", key);
    new KeyValueStore().delete(key);
  }
}
