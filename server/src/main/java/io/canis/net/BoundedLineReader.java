package io.canis.net;

import java.io.BufferedReader;
import java.io.IOException;

public final class BoundedLineReader {

  public static final int MAX_LINE_CHARS = 1_048_576;

  private BoundedLineReader() {
  }

  public static String readLine(BufferedReader reader) throws IOException {
    return readLine(reader, MAX_LINE_CHARS);
  }

  static String readLine(BufferedReader reader, int maxLineChars) throws IOException {
    StringBuilder builder = new StringBuilder();
    int current;

    while ((current = reader.read()) != -1) {
      if (current == '\n') {
        break;
      }

      if (current == '\r') {
        reader.mark(1);
        int next = reader.read();
        if (next != '\n' && next != -1) {
          reader.reset();
        }
        break;
      }

      if (builder.length() >= maxLineChars) {
        throw new LineTooLongException(maxLineChars);
      }
      builder.append((char) current);
    }

    if (current == -1 && builder.isEmpty()) {
      return null;
    }
    return builder.toString();
  }

  public static final class LineTooLongException extends IOException {

    LineTooLongException(int maxLineChars) {
      super("Request line exceeds maximum length of " + maxLineChars + " characters.");
    }
  }
}
