package io.canis.client;

import io.canis.models.Entry;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SocketClient is responsible for low-level communication with the server. It sends commands to the
 * server and retrieves responses in various formats, including String, Map<String, Entry>, and
 * Entry objects. This class handles the socket connection, data transmission, and deserialization
 * of received data.
 */

final class SocketClient {

  private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);
  private final String address;
  private final int port;
  private final String username;
  private final String password;

  public SocketClient(String address, int port, String username, String password)
      throws IOException {
    this.address = address;
    this.port = port;
    this.username = username;
    this.password = password;
    this.auth();
  }

  private void auth() throws IOException {
    try (Socket socket = new Socket(address, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {

      out.println(String.format("|login %s:%s", this.username, this.password));

      int length = dataInputStream.readInt();
      if (length <= 0) {
        throw new IOException("Invalid response from server.");
      }

      byte[] bytes = new byte[length];
      dataInputStream.readFully(bytes);

      logger.info(new String(bytes, StandardCharsets.UTF_8));

    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      throw e;
    }
  }

  /**
   * Sends a command to the server and retrieves the response as a String.
   *
   * @param command the command to send to the server
   * @return the response from the server as a String
   * @throws IOException if an error occurs during communication
   */
  public String getString(String command) throws IOException {

    try (Socket socket = new Socket(address, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream)) {

      out.println(command);

      int length = dataInputStream.readInt();
      byte[] bytes = new byte[length];
      dataInputStream.readFully(bytes);

      return new String(bytes, StandardCharsets.UTF_8);
    }
  }

  public Map<String, Entry> getMap(String command) throws Exception {

    try (Socket socket = new Socket(address, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream)) {

      out.println(command);

      int length = dataInputStream.readInt();
      byte[] bytes = new byte[length];
      dataInputStream.readFully(bytes);

      return deserializeBytesToMap(bytes);
    }
  }

  public Entry getObject(String command) throws Exception {

    try (Socket socket = new Socket(address, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream)) {

      out.println(command);

      int length = dataInputStream.readInt();
      byte[] bytes = new byte[length];
      dataInputStream.readFully(bytes);

      return deserializeByteArrayToEntry(bytes);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Entry> deserializeBytesToMap(byte[] bytes) throws Exception {
    try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {

      Object stream = objectStream.readObject();
      if (stream instanceof Map) {
        return (Map<String, Entry>) stream;
      } else {
        throw new IllegalArgumentException("Unexpected type: " + stream.getClass().getName());
      }
    }
  }

  private Entry deserializeByteArrayToEntry(byte[] bytes) throws Exception {
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

      return (Entry) objectInputStream.readObject();
    }
  }
}
