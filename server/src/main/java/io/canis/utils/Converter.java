package io.canis.utils;


import io.canis.store.Entry;
import java.util.List;
import java.util.Map;

public class Converter {

  public static String mapToString(Map<String, Object> map) {

    StringBuilder builder = new StringBuilder();

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (value instanceof String) {
        builder.append("|ms>").append(key).append(":").append(value);
      } else if (value instanceof Integer) {
        builder.append("|mi>").append(key).append(":").append(value);
      }

    }

    return builder.toString();
  }

  public static String arrayOfMapsToString(List<Entry> entries) {
    StringBuilder builder = new StringBuilder();

    builder.append("|a>");

    for (Entry entry : entries) {
      builder.append("|ms>name:").append(entry.getName());
      builder.append("|ms>publicKey:").append(entry.getPublicKey());

      if (entries.size() > 1) {
        builder.append("|a>");
      }
    }

    return builder.toString();
  }
}
