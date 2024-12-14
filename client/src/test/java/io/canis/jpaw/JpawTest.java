package io.canis.jpaw;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.canis.jpaw.client.Jpaw;
import io.canis.jpaw.utils.Parser;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class JpawTest {

  @Mock
  Jpaw jPaw;

  @Test
  void testLoginCommandError() throws IOException {
    var expected = "Authentication failed";
    when(jPaw.set(anyString())).thenReturn(expected);
    String resp = jPaw.set(anyString());
    assertEquals(expected, resp);
  }

  @Test
  void testLoginCommandOK() throws IOException {
    var expected = "Authentication successful";
    when(jPaw.set(anyString())).thenReturn(expected);
    String resp = jPaw.set(anyString());
    assertEquals(expected, resp);
  }

  @Test
  void testHealthCommand() throws IOException {
    var expected = "OK";
    when(jPaw.health()).thenReturn("|s>OK");
    String result = jPaw.health();
    assertEquals(expected, Parser.parseString(result));
  }

  @Test
  void testAddCommand() throws IOException {
    var expected = "OK";
    when(jPaw.set(anyString())).thenReturn("OK");
    String result = jPaw.set(anyString());
    assertEquals(expected, result);
  }

  @Test
  void testGetCommand() throws IOException {
    when(jPaw.get(anyString())).thenReturn(Map.of("name", "Mikey"));
    Map<String, Object> result = jPaw.get("Mikey");
    assertEquals("Mikey", result.get("name"));
  }

  @Test
  void testDeleteCommand() throws IOException {
    when(jPaw.delete(anyString())).thenReturn(true);
    boolean result = jPaw.delete("Alice");
    assertTrue(result);
  }
}


