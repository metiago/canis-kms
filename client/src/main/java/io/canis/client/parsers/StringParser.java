package io.canis.client.parsers;

public class StringParser {

  public static String parseString(String s) {
    return s.replace("|s>", "");
  }
}
