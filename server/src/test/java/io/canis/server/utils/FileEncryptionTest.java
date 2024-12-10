package io.canis.server.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.canis.utils.FileDecryptor;
import io.canis.utils.FileEncryptor;
import io.canis.utils.AsymmetricPairGenerator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileEncryptionTest {

  private static final String INPUT_FILE_PATH = System.getProperty("user.dir") + "\\content.txt";
  private static final String ENCRYPTED_FILE_PATH = System.getProperty("user.dir") + "\\file.enc";
  private static final String DECRYPTED_FILE_PATH = System.getProperty("user.dir") + "\\dec_file.txt";

  private PublicKey publicKey;
  private PrivateKey privateKey;

  @BeforeEach
  public void setUp() throws NoSuchAlgorithmException {
    KeyPair keyPair = AsymmetricPairGenerator.generateKeyPair();
    publicKey = keyPair.getPublic();
    privateKey = keyPair.getPrivate();
    cleanUpFiles();
  }

  @Test
  public void testEncryptDecryptFile() throws Exception {

    String originalContent = "This is a test content for encryption.";
    writeToFile(INPUT_FILE_PATH, originalContent);

    File inputFile = new File(INPUT_FILE_PATH);
    File encryptedFile = new File(ENCRYPTED_FILE_PATH);
    FileEncryptor.encryptFile(inputFile, encryptedFile, publicKey);

    File decryptedFile = new File(DECRYPTED_FILE_PATH);
    FileDecryptor.decryptFile(encryptedFile, decryptedFile, privateKey);

    String decryptedContent = new String(Files.readAllBytes(decryptedFile.toPath()));
    assertEquals(originalContent, decryptedContent, "The decrypted content should match the original content.");
  }

  private void writeToFile(String filePath, String content) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
      writer.write(content);
    }
  }

  private void cleanUpFiles() {
    deleteFile(INPUT_FILE_PATH);
    deleteFile(ENCRYPTED_FILE_PATH);
    deleteFile(DECRYPTED_FILE_PATH);
  }

  private void deleteFile(String filePath) {
    File file = new File(filePath);
    if (file.exists()) {
      file.delete();
    }
  }

  @AfterEach
  public void tearDown() {
    cleanUpFiles();
  }
}

