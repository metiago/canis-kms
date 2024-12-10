package io.canis.client.parsers;

public class IntParser {

  public static Integer parseInt(String input) {
    String s = input.replace("|i>", "");
    return Integer.parseInt(s);
  }
}
