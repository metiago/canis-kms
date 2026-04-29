package io.canis.jpaw.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

public final class EnvelopeCryptographer {

  private static final String RSA_OAEP_TRANSFORMATION =
      "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
  private static final String RSA_ALGORITHM = "RSA";
  private static final String AES_ALGORITHM = "AES";
  private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
  private static final byte[] HYBRID_ENVELOPE_MAGIC =
      "CANISHYB1".getBytes(StandardCharsets.US_ASCII);
  private static final int AES_KEY_LENGTH_BITS = 256;
  private static final int GCM_NONCE_LENGTH_BYTES = 12;
  private static final int GCM_TAG_LENGTH_BYTES = 16;
  private static final int GCM_TAG_LENGTH_BITS = 128;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final OAEPParameterSpec RSA_OAEP_PARAMETER_SPEC = new OAEPParameterSpec(
      "SHA-256",
      "MGF1",
      MGF1ParameterSpec.SHA256,
      PSource.PSpecified.DEFAULT);

  private EnvelopeCryptographer() {
  }

  @FunctionalInterface
  public interface DataKeyDecryptor {
    byte[] decrypt(byte[] encryptedDataKey) throws IOException;
  }

  public static PublicKey publicKeyFromString(String publicKey) throws GeneralSecurityException {
    byte[] decodedKey = Base64.getDecoder().decode(publicKey);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
    return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(spec);
  }

  public static void encryptFile(File inputFile, File outputFile, PublicKey publicKey)
      throws IOException, GeneralSecurityException {

    byte[] plaintext = Files.readAllBytes(inputFile.toPath());
    byte[] envelope = encryptHybridEnvelope(plaintext, publicKey);
    Files.write(outputFile.toPath(), envelope);
  }

  public static void decryptFile(
      File inputFile,
      File outputFile,
      DataKeyDecryptor dataKeyDecryptor) throws IOException, GeneralSecurityException {

    byte[] envelopeBytes = Files.readAllBytes(inputFile.toPath());
    byte[] plaintext = decryptHybridEnvelope(envelopeBytes, dataKeyDecryptor);
    Files.write(outputFile.toPath(), plaintext);
  }

  private static byte[] encryptHybridEnvelope(byte[] plaintext, PublicKey publicKey)
      throws GeneralSecurityException, IOException {

    SecretKey dataKey = generateDataEncryptionKey();
    byte[] nonce = new byte[GCM_NONCE_LENGTH_BYTES];
    SECURE_RANDOM.nextBytes(nonce);

    Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, dataKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce));
    byte[] ciphertextWithTag = cipher.doFinal(plaintext);

    int ciphertextLength = ciphertextWithTag.length - GCM_TAG_LENGTH_BYTES;
    byte[] ciphertext = Arrays.copyOf(ciphertextWithTag, ciphertextLength);
    byte[] tag = Arrays.copyOfRange(ciphertextWithTag, ciphertextLength, ciphertextWithTag.length);
    byte[] encryptedDataKey = encryptDataKey(dataKey.getEncoded(), publicKey);

    try (ByteArrayOutputStream output = new ByteArrayOutputStream();
        DataOutputStream envelope = new DataOutputStream(output)) {
      envelope.write(HYBRID_ENVELOPE_MAGIC);
      envelope.writeInt(encryptedDataKey.length);
      envelope.writeInt(nonce.length);
      envelope.writeInt(ciphertext.length);
      envelope.writeInt(tag.length);
      envelope.write(encryptedDataKey);
      envelope.write(nonce);
      envelope.write(ciphertext);
      envelope.write(tag);
      return output.toByteArray();
    }
  }

  private static byte[] decryptHybridEnvelope(byte[] envelopeBytes, DataKeyDecryptor decryptor)
      throws IOException, GeneralSecurityException {

    try (DataInputStream envelope =
        new DataInputStream(new ByteArrayInputStream(envelopeBytes))) {
      byte[] magic = new byte[HYBRID_ENVELOPE_MAGIC.length];
      envelope.readFully(magic);
      if (!Arrays.equals(magic, HYBRID_ENVELOPE_MAGIC)) {
        throw new IOException("Invalid CANIS envelope.");
      }

      int encryptedDataKeyLength = envelope.readInt();
      int nonceLength = envelope.readInt();
      int ciphertextLength = envelope.readInt();
      int tagLength = envelope.readInt();
      validateHybridEnvelopeLengths(
          envelopeBytes.length, encryptedDataKeyLength, nonceLength, ciphertextLength, tagLength);

      byte[] encryptedDataKey = new byte[encryptedDataKeyLength];
      byte[] nonce = new byte[nonceLength];
      byte[] ciphertext = new byte[ciphertextLength];
      byte[] tag = new byte[tagLength];
      envelope.readFully(encryptedDataKey);
      envelope.readFully(nonce);
      envelope.readFully(ciphertext);
      envelope.readFully(tag);

      byte[] dataKeyBytes = decryptor.decrypt(encryptedDataKey);
      try {
        SecretKey dataKey = new SecretKeySpec(dataKeyBytes, AES_ALGORITHM);
        byte[] ciphertextWithTag = new byte[ciphertext.length + tag.length];
        System.arraycopy(ciphertext, 0, ciphertextWithTag, 0, ciphertext.length);
        System.arraycopy(tag, 0, ciphertextWithTag, ciphertext.length, tag.length);

        Cipher cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, dataKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce));
        return cipher.doFinal(ciphertextWithTag);
      } finally {
        Arrays.fill(dataKeyBytes, (byte) 0);
      }
    }
  }

  private static byte[] encryptDataKey(byte[] dataKey, PublicKey publicKey)
      throws GeneralSecurityException {

    Cipher cipher = Cipher.getInstance(RSA_OAEP_TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, publicKey, RSA_OAEP_PARAMETER_SPEC);
    return cipher.doFinal(dataKey);
  }

  private static SecretKey generateDataEncryptionKey() throws GeneralSecurityException {
    KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
    keyGenerator.init(AES_KEY_LENGTH_BITS);
    return keyGenerator.generateKey();
  }

  private static void validateHybridEnvelopeLengths(
      int envelopeLength,
      int encryptedDataKeyLength,
      int nonceLength,
      int ciphertextLength,
      int tagLength) throws IOException {

    if (encryptedDataKeyLength <= 0) {
      throw new IOException("Invalid hybrid envelope: encrypted data key is missing.");
    }
    if (nonceLength != GCM_NONCE_LENGTH_BYTES) {
      throw new IOException("Invalid hybrid envelope: unexpected nonce length.");
    }
    if (ciphertextLength < 0) {
      throw new IOException("Invalid hybrid envelope: ciphertext length is negative.");
    }
    if (tagLength != GCM_TAG_LENGTH_BYTES) {
      throw new IOException("Invalid hybrid envelope: unexpected authentication tag length.");
    }

    long headerLength = HYBRID_ENVELOPE_MAGIC.length + (4L * Integer.BYTES);
    long expectedLength = headerLength
        + encryptedDataKeyLength
        + nonceLength
        + ciphertextLength
        + tagLength;
    if (expectedLength != envelopeLength) {
      throw new IOException("Invalid hybrid envelope: length fields do not match payload.");
    }
  }
}
