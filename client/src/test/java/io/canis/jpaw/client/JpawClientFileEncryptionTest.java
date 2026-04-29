package io.canis.jpaw.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpawClientFileEncryptionTest {

  private static final String RSA_OAEP_TRANSFORMATION =
      "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
  private static final OAEPParameterSpec RSA_OAEP_PARAMETER_SPEC = new OAEPParameterSpec(
      "SHA-256",
      "MGF1",
      MGF1ParameterSpec.SHA256,
      PSource.PSpecified.DEFAULT);

  @TempDir
  Path tempDir;

  @Mock
  SocketClient socketClient;

  @Test
  void testEncryptAndDecryptFileThroughServerSideKeyUnwrap() throws Exception {
    KeyPair keyPair = generateKeyPair();
    String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    JpawClient client = new JpawClient(socketClient);
    Path inputFile = tempDir.resolve("shared.txt");
    Path encryptedFile = tempDir.resolve("shared.enc");
    Path decryptedFile = tempDir.resolve("shared.dec.txt");
    String originalContent = "shared file content\n".repeat(200);
    Files.writeString(inputFile, originalContent, StandardCharsets.UTF_8);

    when(socketClient.sendCommand(eq("|get serviceA")))
        .thenReturn("|ms>name:serviceA|ms>publicKey:" + publicKey);
    when(socketClient.sendCommand(startsWith("|decrypt serviceA ")))
        .thenAnswer(invocation -> {
          String command = invocation.getArgument(0);
          String encryptedDataKeyBase64 = command.split("\\s+", 3)[2];
          byte[] encryptedDataKey = Base64.getDecoder().decode(encryptedDataKeyBase64);
          byte[] dataKey = decryptDataKey(encryptedDataKey, keyPair.getPrivate());
          return "|s>" + Base64.getEncoder().encodeToString(dataKey);
        });

    client.encryptFile("serviceA", inputFile.toFile(), encryptedFile.toFile());
    client.decryptFile("serviceA", encryptedFile.toFile(), decryptedFile.toFile());

    assertEquals(originalContent, Files.readString(decryptedFile, StandardCharsets.UTF_8));
    verify(socketClient).sendCommand("|get serviceA");
    verify(socketClient).sendCommand(startsWith("|decrypt serviceA "));
  }

  private KeyPair generateKeyPair() throws GeneralSecurityException {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    return generator.generateKeyPair();
  }

  private byte[] decryptDataKey(byte[] encryptedDataKey, PrivateKey privateKey)
      throws GeneralSecurityException {

    Cipher cipher = Cipher.getInstance(RSA_OAEP_TRANSFORMATION);
    cipher.init(Cipher.DECRYPT_MODE, privateKey, RSA_OAEP_PARAMETER_SPEC);
    return cipher.doFinal(encryptedDataKey);
  }
}
