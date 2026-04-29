package io.canis.server.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.canis.utils.AsymmetricGenerator;
import io.canis.utils.Cryptographer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileEncryptionTest {

  private static final byte[] HYBRID_ENVELOPE_MAGIC =
      "CANISHYB1".getBytes(StandardCharsets.US_ASCII);

  @TempDir
  Path tempDir;

  private PublicKey publicKey;
  private PrivateKey privateKey;
  private Path inputFile;
  private Path encryptedFile;
  private Path decryptedFile;

  @BeforeEach
  public void setUp() throws NoSuchAlgorithmException {
    KeyPair keyPair = AsymmetricGenerator.generateKeyPair();
    publicKey = keyPair.getPublic();
    privateKey = keyPair.getPrivate();
    inputFile = tempDir.resolve("content.txt");
    encryptedFile = tempDir.resolve("file.enc");
    decryptedFile = tempDir.resolve("dec_file.txt");
  }

  @Test
  public void testEncryptDecryptFileUsesHybridEnvelope() throws Exception {

    String originalContent = "This is a test content for encryption.\n".repeat(200);
    Files.writeString(inputFile, originalContent, StandardCharsets.UTF_8);

    Cryptographer.encryptFile(inputFile.toFile(), encryptedFile.toFile(), publicKey);

    byte[] encryptedBytes = Files.readAllBytes(encryptedFile);
    assertStartsWith(encryptedBytes, HYBRID_ENVELOPE_MAGIC);

    Cryptographer.decryptFile(encryptedFile.toFile(), decryptedFile.toFile(), privateKey);

    String decryptedContent = Files.readString(decryptedFile, StandardCharsets.UTF_8);
    assertEquals(originalContent, decryptedContent, "The decrypted content should match the original content.");
  }

  @Test
  public void testDecryptFileReadsLegacyDirectRsaPayload() throws Exception {

    byte[] plaintext = "legacy payload".getBytes(StandardCharsets.UTF_8);
    byte[] legacyCiphertext = Cryptographer.encrypt(plaintext, publicKey);
    Files.write(encryptedFile, legacyCiphertext);

    Cryptographer.decryptFile(encryptedFile.toFile(), decryptedFile.toFile(), privateKey);

    assertEquals("legacy payload", Files.readString(decryptedFile, StandardCharsets.UTF_8));
  }

  private void assertStartsWith(byte[] data, byte[] prefix) {
    assertTrue(data.length >= prefix.length);
    for (int i = 0; i < prefix.length; i++) {
      assertEquals(prefix[i], data[i]);
    }
  }
}

