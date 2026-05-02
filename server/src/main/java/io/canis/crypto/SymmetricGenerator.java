package io.canis.crypto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SymmetricGenerator {

  private static final String KEY_ALGORITHM = "AES";
  private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
  private static final byte[] GCM_MAGIC = "CANISGCM1".getBytes(StandardCharsets.US_ASCII);
  private static final int GCM_IV_LENGTH_BYTES = 12;
  private static final int GCM_TAG_LENGTH_BITS = 128;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private SecretKey secretKey;

  private final String secretKeyPath;

  public SymmetricGenerator() throws NoSuchAlgorithmException, IOException {
    this(System.getenv("CANIS_SECRET_KEY"));
  }

  public SymmetricGenerator(String secretKeyPath) throws NoSuchAlgorithmException, IOException {
    this.secretKeyPath = secretKeyPath;
    this.generatePrivateKey();
  }

  private void generatePrivateKey() throws NoSuchAlgorithmException, IOException {
    if (secretKeyPath == null || secretKeyPath.isBlank()) {
      throw new IOException("CANIS_SECRET_KEY is not set");
    }
    if (!new File(secretKeyPath).exists()) {
      this.secretKey = generateSecretKey();
      String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
      saveKeyToFile(encodedKey);
    } else {
      byte[] keyBytes = Base64.getDecoder().decode(loadKeyFromFile());
      this.secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }
  }

  public SecretKey generateSecretKey() throws NoSuchAlgorithmException {
    KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);
    keyGen.init(128); // 192 - 256
    return keyGen.generateKey();
  }

  public byte[] encrypt(byte[] data) throws Exception {
    byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
    SECURE_RANDOM.nextBytes(iv);

    Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
    byte[] ciphertext = cipher.doFinal(data);

    byte[] encryptedData = new byte[GCM_MAGIC.length + iv.length + ciphertext.length];
    System.arraycopy(GCM_MAGIC, 0, encryptedData, 0, GCM_MAGIC.length);
    System.arraycopy(iv, 0, encryptedData, GCM_MAGIC.length, iv.length);
    System.arraycopy(ciphertext, 0, encryptedData, GCM_MAGIC.length + iv.length, ciphertext.length);
    return encryptedData;
  }

  public byte[] decrypt(byte[] data) throws Exception {
    if (!isGcmEnvelope(data)) {
      throw new IOException("Invalid CANIS encrypted data format.");
    }

    byte[] iv = Arrays.copyOfRange(data, GCM_MAGIC.length, GCM_MAGIC.length + GCM_IV_LENGTH_BYTES);
    byte[] ciphertext = Arrays.copyOfRange(data, GCM_MAGIC.length + GCM_IV_LENGTH_BYTES, data.length);

    Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
    return cipher.doFinal(ciphertext);
  }

  private boolean isGcmEnvelope(byte[] data) {
    if (data.length <= GCM_MAGIC.length + GCM_IV_LENGTH_BYTES) {
      return false;
    }

    for (int i = 0; i < GCM_MAGIC.length; i++) {
      if (data[i] != GCM_MAGIC[i]) {
        return false;
      }
    }
    return true;
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
