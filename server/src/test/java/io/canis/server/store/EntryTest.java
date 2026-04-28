package io.canis.server.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.canis.store.Entry;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class EntryTest {

  @Test
  void testToMapDoesNotExposePrivateKey() {
    Entry entry = new Entry("app", "public-key", "private-key");

    Map<String, Object> result = entry.toMap();

    assertEquals("app", result.get("name"));
    assertEquals("public-key", result.get("publicKey"));
    assertFalse(result.containsKey("privateKey"));
  }
}
