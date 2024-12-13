package io.canis.client;

import java.io.IOException;
import java.util.Map;

public interface Canis {
  String health() throws IOException;
  String set(String key) throws IOException;
  Map<String, Object> get(String key) throws IOException;
  boolean delete(String key) throws IOException;
  String list() throws IOException;
}

