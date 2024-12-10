package io.canis.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.canis.client.parsers.IntParser;
import io.canis.client.parsers.MapParser;
import io.canis.client.parsers.StringParser;
import org.junit.jupiter.api.Test;

class ProtocolParserTest {

  @Test
  void testParsingMap() {
    var k = "|a>|ms>name:john|mi>age:25|ms>city:poa|ms>state:rs";
    var result = MapParser.parseMap(k);
    assertNotNull(result, "Parsed map should not be null");
    assertEquals("john", result.get("name"));
    assertEquals(25, result.get("age"));
    assertEquals("poa", result.get("city"));
    assertEquals("rs", result.get("state"));
  }

  @Test
  void testParsingMapError() {
    var k = "|as>|m>name:john|mi>age:25|ms>city:poa|ms>state:rs";
    var result = MapParser.parseMap(k);
    assertNotNull(result, "Parsed map should not be null");
    assertEquals(null, result.get("name"));
    assertEquals(25, result.get("age"));
    assertEquals("poa", result.get("city"));
    assertEquals("rs", result.get("state"));
  }

  @Test
  void testStringParsing() {
    var message = "|s>Hello World";
    var result = StringParser.parseString(message);
    assertEquals("Hello World", result, "Parsed string does not match expected value");
  }

  @Test
  void testIntegerParsing() {
    var message = "|i>100";
    var result = IntParser.parseInt(message);
    assertEquals(100, result, "Parsed integer does not match expected value");
  }
}

