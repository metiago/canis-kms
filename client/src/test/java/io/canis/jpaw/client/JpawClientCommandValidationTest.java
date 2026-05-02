package io.canis.jpaw.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpawClientCommandValidationTest {

  @Mock
  SocketClient socketClient;

  @Test
  void testSetValidatesOkResponse() throws Exception {
    JpawClient client = new JpawClient(socketClient);
    when(socketClient.sendCommand("|set serviceA")).thenReturn("|s>OK");

    assertEquals("OK", client.set("serviceA"));
  }

  @Test
  void testSetRejectsUnexpectedResponse() throws Exception {
    JpawClient client = new JpawClient(socketClient);
    when(socketClient.sendCommand("|set serviceA")).thenReturn("|s>NO");

    assertThrows(IOException.class, () -> client.set("serviceA"));
  }

  @Test
  void testDeleteValidatesOkResponse() throws Exception {
    JpawClient client = new JpawClient(socketClient);
    when(socketClient.sendCommand("|del serviceA")).thenReturn("|s>OK");

    assertTrue(client.delete("serviceA"));
  }

  @Test
  void testDeleteRejectsServerError() throws Exception {
    JpawClient client = new JpawClient(socketClient);
    when(socketClient.sendCommand("|del serviceA")).thenReturn("|s>ERROR: Delete failed");

    assertThrows(IOException.class, () -> client.delete("serviceA"));
  }

  @Test
  void testHealthValidatesOkResponse() throws Exception {
    JpawClient client = new JpawClient(socketClient);
    when(socketClient.sendCommand("|health")).thenReturn("|s>OK");

    assertEquals("OK", client.health());
  }

  @Test
  void testKeyCommandsRejectInvalidServiceNamesBeforeSending() {
    JpawClient client = new JpawClient(socketClient);
    String[] invalidNames = {
        null,
        "",
        " ",
        "service A",
        "service\tA",
        "service\nA",
        "service|A",
        "service:A",
        "a".repeat(129)
    };

    for (String serviceName : invalidNames) {
      assertRejectsInvalidServiceName(() -> client.set(serviceName));
      assertRejectsInvalidServiceName(() -> client.get(serviceName));
      assertRejectsInvalidServiceName(() -> client.getPublicKey(serviceName));
      assertRejectsInvalidServiceName(() -> client.decrypt(serviceName, new byte[] {1, 2, 3}));
      assertRejectsInvalidServiceName(
          () -> client.encryptFile(serviceName, new File("input.txt"), new File("encrypted.dat")));
      assertRejectsInvalidServiceName(
          () -> client.decryptFile(serviceName, new File("encrypted.dat"), new File("output.txt")));
      assertRejectsInvalidServiceName(() -> client.delete(serviceName));
    }

    verifyNoInteractions(socketClient);
  }

  private void assertRejectsInvalidServiceName(ThrowingCall call) {
    assertThrows(IllegalArgumentException.class, call::run);
  }

  @FunctionalInterface
  private interface ThrowingCall {
    void run() throws Exception;
  }
}
