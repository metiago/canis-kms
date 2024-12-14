package io.canis.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.Cipher;

public class Cryptographer {

  public static void decryptFile(File inputFile, File outputFile, PrivateKey privateKey) throws Exception {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.DECRYPT_MODE, privateKey);

      try (FileInputStream fis = new FileInputStream(inputFile);
           FileOutputStream fos = new FileOutputStream(outputFile)) {
          byte[] inputBytes = new byte[(int) inputFile.length()];
          fis.read(inputBytes);

          byte[] outputBytes = cipher.doFinal(inputBytes);
          fos.write(outputBytes);
      }
  }

  public static void encryptFile(File inputFile, File outputFile, PublicKey publicKey) throws Exception {
      Cipher cipher = Cipher.getInstance("RSA");
      cipher.init(Cipher.ENCRYPT_MODE, publicKey);

      try (FileInputStream fis = new FileInputStream(inputFile);
           FileOutputStream fos = new FileOutputStream(outputFile)) {
          byte[] inputBytes = new byte[(int) inputFile.length()];
          fis.read(inputBytes);

          byte[] outputBytes = cipher.doFinal(inputBytes);
          fos.write(outputBytes);
      }
  }
}
