package io.canis.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;

public class Cryptographer {

  public static byte[] decrypt(byte[] inputBytes, PrivateKey privateKey)
      throws GeneralSecurityException {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.DECRYPT_MODE, privateKey);
      return cipher.doFinal(inputBytes);
  }

  public static byte[] encrypt(byte[] inputBytes, PublicKey publicKey)
      throws GeneralSecurityException {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);
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
