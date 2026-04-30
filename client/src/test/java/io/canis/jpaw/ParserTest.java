package io.canis.jpaw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.canis.jpaw.utils.Parser;
import org.junit.jupiter.api.Test;

class ParserTest {

  @Test
  void testParsingMap() {
    var k = "|a>|ms>name:john|mi>age:25|ms>city:poa|ms>state:rs";
    var result = Parser.parseMap(k);
    assertNotNull(result, "Parsed map should not be null");
    assertEquals("john", result.get("name"));
    assertEquals(25, result.get("age"));
    assertEquals("poa", result.get("city"));
    assertEquals("rs", result.get("state"));
  }

  @Test
  void testParsingArrayOfMaps() {
    var message = "|a>|ms>name:serviceA|ms>publicKey:public-key-a"
        + "|a>|ms>name:serviceB|ms>publicKey:public-key-b";

    var result = Parser.parseArray(message);

    assertEquals(2, result.size());
    assertEquals("serviceA", result.get(0).get("name"));
    assertEquals("public-key-a", result.get(0).get("publicKey"));
    assertEquals("serviceB", result.get(1).get("name"));
    assertEquals("public-key-b", result.get(1).get("publicKey"));
  }

  @Test
  void testParsingMapError() {
    var k = "|as>|m>name:john|mi>age:25|ms>city:poa|ms>state:rs";
    var result = Parser.parseMap(k);
    assertNotNull(result, "Parsed map should not be null");
    assertNull(result.get("name"));
    assertEquals(25, result.get("age"));
    assertEquals("poa", result.get("city"));
    assertEquals("rs", result.get("state"));
  }

  @Test
  void testStringParsing() {
    var message = "|s>Hello World";
    var result = Parser.parseString(message);
    assertEquals("Hello World", result, "Parsed string does not match expected value");
  }

  @Test
  void testStringParsingOnlyRemovesLeadingPrefix() {
    var message = "|s>Hello |s> World";
    var result = Parser.parseString(message);
    assertEquals("Hello |s> World", result);
  }

  @Test
  void testIntegerParsing() {
    var message = "|i>100";
    var result = Parser.parseInt(message);
    assertEquals(100, result, "Parsed integer does not match expected value");
  }

  @Test
  void testParsingMapValueContainingColon() {
    var message = "|ms>name:service:with:colon|mi>age:25";

    var result = Parser.parseMap(message);

    assertEquals("service:with:colon", result.get("name"));
    assertEquals(25, result.get("age"));
  }

  @Test
  void testParsingMapIgnoresEmptyFieldsAndUnknownTypes() {
    var message = "|ms>|mx>unknown:value|mi>age:25|ms>name:serviceA";

    var result = Parser.parseMap(message);

    assertNull(result.get(""));
    assertNull(result.get("unknown"));
    assertEquals(25, result.get("age"));
    assertEquals("serviceA", result.get("name"));
  }

  @Test
  void testParsingMapIgnoresInvalidInteger() {
    var message = "|mi>age:not-a-number|ms>name:serviceA";

    var result = Parser.parseMap(message);

    assertNull(result.get("age"));
    assertEquals("serviceA", result.get("name"));
  }

  @Test
  void testParsingInvalidIntegerThrows() {
    assertThrows(NumberFormatException.class, () -> Parser.parseInt("|i>not-a-number"));
  }
}

