package io.canis.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class Cryptographer {

  private static final String RSA_OAEP_TRANSFORMATION =
      "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
  private static final OAEPParameterSpec RSA_OAEP_PARAMETER_SPEC = new OAEPParameterSpec(
      "SHA-256",
      "MGF1",
      MGF1ParameterSpec.SHA256,
      PSource.PSpecified.DEFAULT);

  public static byte[] decrypt(byte[] inputBytes, PrivateKey privateKey)
      throws GeneralSecurityException {
      Cipher cipher = Cipher.getInstance(RSA_OAEP_TRANSFORMATION);
      cipher.init(Cipher.DECRYPT_MODE, privateKey, RSA_OAEP_PARAMETER_SPEC);
      return cipher.doFinal(inputBytes);
  }

  public static byte[] encrypt(byte[] inputBytes, PublicKey publicKey)
      throws GeneralSecurityException {
      Cipher cipher = Cipher.getInstance(RSA_OAEP_TRANSFORMATION);
      cipher.init(Cipher.ENCRYPT_MODE, publicKey, RSA_OAEP_PARAMETER_SPEC);
      return cipher.doFinal(inputBytes);
  }

  public static void decryptFile(File inputFile, File outputFile, PrivateKey privateKey) throws Exception {
      try (FileInputStream fis = new FileInputStream(inputFile);
           FileOutputStream fos = new FileOutputStream(outputFile)) {
          byte[] inputBytes = new byte[(int) inputFile.length()];
          fis.read(inputBytes);

          byte[] outputBytes = decrypt(inputBytes, privateKey);
          fos.write(outputBytes);
      }
  }

  public static void encryptFile(File inputFile, File outputFile, PublicKey publicKey) throws Exception {
      try (FileInputStream fis = new FileInputStream(inputFile);
           FileOutputStream fos = new FileOutputStream(outputFile)) {
          byte[] inputBytes = new byte[(int) inputFile.length()];
          fis.read(inputBytes);

          byte[] outputBytes = encrypt(inputBytes, publicKey);
          fos.write(outputBytes);
      }
  }
}
