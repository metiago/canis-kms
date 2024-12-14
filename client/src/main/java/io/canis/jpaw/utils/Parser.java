package io.canis.jpaw.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

  public static String parseString(String s) {
    return s.replace("|s>", "");
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
    String[] keyValue = part.substring(2).split(":");
    if (keyValue.length == 2) {
      map.put(keyValue[0].trim(), keyValue[1].trim());
    }
  }

  private static void processIntMapEntry(String part, Map<String, Object> map) {
    String[] keyValue = part.substring(2).split(":");
    if (keyValue.length == 2) {
      try {
        map.put(keyValue[0].trim(), Integer.parseInt(keyValue[1].trim()));
      } catch (NumberFormatException e) {
        System.out.println("Invalid integer format for key: " + keyValue[0]);
      }
    }
  }

  public static Integer parseInt(String input) {
    String s = input.replace("|i>", "");
    return Integer.parseInt(s);
  }
}
