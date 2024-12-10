package io.canis.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SymmetricKeyGenerator {

  private static final Logger logger = LoggerFactory.getLogger(SymmetricKeyGenerator.class);

  private SecretKey secretKey;

  private final String secretKeyPath;

  public SymmetricKeyGenerator() throws NoSuchAlgorithmException, IOException {
    this.secretKeyPath = System.getenv("CANIS_SECRET_KEY");
    this.generatePrivateKey();
  }

  private void generatePrivateKey() throws NoSuchAlgorithmException, IOException {
    if (!new File(secretKeyPath).exists()) {
      this.secretKey = generateSecretKey();
      String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
      saveKeyToFile(encodedKey);
    } else {
      byte[] keyBytes = Base64.getDecoder().decode(loadKeyFromFile());
      this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }
  }

  public SecretKey generateSecretKey() throws NoSuchAlgorithmException {
    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
    keyGen.init(128); // 192 - 256
    return keyGen.generateKey();
  }

  public byte[] encrypt(byte[] data) throws Exception {
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    return cipher.doFinal(data);
  }

  public byte[] decrypt(byte[] data) throws Exception {
    Cipher cipher = Cipher.getInstance("AES");
    cipher.init(Cipher.DECRYPT_MODE, secretKey);
    return cipher.doFinal(data);
  }

  private void saveKeyToFile(String keyString) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(secretKeyPath))) {
      writer.write(keyString);
    }
  }

  private String loadKeyFromFile() throws IOException {
    StringBuilder keyString = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new FileReader(secretKeyPath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        keyString.append(line);
      }
    }
    return keyString.toString();
  }

}
