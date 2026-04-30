package io.canis.jpaw.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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
}
