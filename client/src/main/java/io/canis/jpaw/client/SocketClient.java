package io.canis.jpaw.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * SocketClient is responsible for low-level communication with the server. This class handles the
 * socket connection, data transmission, and deserialization of received data.
 */
final class SocketClient implements AutoCloseable {

  private final String username;
  private final String password;
  private final Socket socket;
  private final PrintWriter out;
  private final DataInputStream dataInputStream;

  public SocketClient(String address, int port, String username, String password)
      throws IOException {
    this.username = username;
    this.password = password;
    this.socket = new Socket(address, port);
    this.out = new PrintWriter(socket.getOutputStream(), true);
    this.dataInputStream = new DataInputStream(socket.getInputStream());
    this.auth();
  }

  private void auth() throws IOException {
    try {

      out.println(String.format("|login %s:%s", this.username, this.password));

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

    out.println(command);
    if (out.checkError()) {
      throw new IOException("Failed to send command to server.");
    }

    return readResponse();
  }

  private String readResponse() throws IOException {
    int length = dataInputStream.readInt();
    if (length <= 0) {
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
