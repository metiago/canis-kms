package io.canis.jpaw.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * SocketClient is responsible for low-level communication with the server. This class handles the
 * socket connection, data transmission, and deserialization of received data.
 */
final class SocketClient {

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

    } catch (IOException e) {
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
  public String sendCommand(String command) throws IOException {

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
}
