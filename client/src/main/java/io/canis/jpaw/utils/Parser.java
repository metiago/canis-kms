package io.canis.jpaw.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

  public static String parseString(String s) {
    if (s == null) {
      return "";
    }
    if (s.startsWith("|s>")) {
      return s.substring(3);
    }
    return s;
  }

  public static List<Map<String, Object>> parseArray(String message) {

    List<Map<String, Object>> resp = new ArrayList<>();

    List<String> arrays = Arrays.stream(message.split("\\|a>")).filter(s -> !s.isEmpty()).toList();

    for (String arrayValue : arrays) {
      resp.add(parseMap(arrayValue));
    }

    return resp;
  }

  public static Map<String, Object> parseMap(String message) {

    String[] parts = message.split("\\|m");

    Map<String, Object> map = new HashMap<>();

    for (String part : parts) {
      if (!part.isEmpty()) {

        if (part.startsWith("s>")) {
          processStringMapEntry(part, map);
        } else if (part.startsWith("i>")) {
          processIntMapEntry(part, map);
        }
      }
    }

    return map;
  }

  private static void processStringMapEntry(String part, Map<String, Object> map) {
    String[] keyValue = part.substring(2).split(":", 2);
    if (keyValue.length == 2 && !keyValue[0].isBlank()) {
      map.put(keyValue[0].trim(), keyValue[1].trim());
    }
  }

  private static void processIntMapEntry(String part, Map<String, Object> map) {
    String[] keyValue = part.substring(2).split(":", 2);
    if (keyValue.length == 2 && !keyValue[0].isBlank()) {
      try {
        map.put(keyValue[0].trim(), Integer.parseInt(keyValue[1].trim()));
      } catch (NumberFormatException e) {
        return;
      }
    }
  }

  public static Integer parseInt(String input) {
    String s = input.startsWith("|i>") ? input.substring(3) : input;
    return Integer.parseInt(s);
  }
}
