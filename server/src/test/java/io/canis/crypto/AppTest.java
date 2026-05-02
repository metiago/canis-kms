//package io.canis.crypto;
//
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import io.canis.Server;
//import io.canis.jpaw.client.JpawClient;
//import io.canis.crypto.Cryptographer;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.security.KeyFactory;
//import java.security.PrivateKey;
//import java.security.PublicKey;
//import java.security.spec.PKCS8EncodedKeySpec;
//import java.security.spec.X509EncodedKeySpec;
//import java.util.Base64;
//import java.util.Map;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//
//public class AppTest {
//
//  private static final String INPUT_FILE_PATH = System.getProperty("user.dir") + "\\content.txt";
//  private static final String ENCRYPTED_FILE_PATH = System.getProperty("user.dir") + "\\file.enc";
//  private static final String DECRYPTED_FILE_PATH =
//      System.getProperty("user.dir") + "\\dec_file.txt";
//
//  private static JpawClient canis;
//
//  @BeforeAll
//  static void serverStart() throws IOException {
//    new Thread(() -> new Server().start()).start();
//    canis = new JpawClient();
//  }
//
//  @Test
//  public void testPowerBuilderGenerateFile() throws Exception {
//    // bmg app registering itself in KMS
//    canis.set("bmg");
//    // application writes a sensitive file
//    String originalContent = "This is a test content for encryption.";
//    writeToFile(originalContent);
//    // application fetches bmg keys
//    Map<String, Object> bmg = canis.get("bmg");
//    // application encrypts before saving it to disk
//    File inputFile = new File(INPUT_FILE_PATH);
//    File encryptedFile = new File(ENCRYPTED_FILE_PATH);
//    Cryptographer.encryptFile(inputFile, encryptedFile,
//        loadPublicKey((String) bmg.get("publicKey")));
//
//    // bmg app decrypt the file using its private key to be able to process it
//    File decryptedFile = new File(DECRYPTED_FILE_PATH);
//    Cryptographer.decryptFile(encryptedFile, decryptedFile,
//        loadPrivateKey((String) bmg.get("privateKey")));
//
//    String decryptedContent = new String(Files.readAllBytes(decryptedFile.toPath()));
//    assertEquals(originalContent, decryptedContent,
//        "The decrypted content should match the original content.");
//  }
//
//  public PublicKey loadPublicKey(String base64PublicKey) throws Exception {
//
//    String publicKeyPEM = base64PublicKey.replace("-----BEGIN PUBLIC KEY-----", "")
//        .replace("-----END PUBLIC KEY-----", "")
//        .replaceAll("\\s+", "");
//    byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
//
//    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
//    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//    return keyFactory.generatePublic(keySpec);
//  }
//
//  public PrivateKey loadPrivateKey(String base64PrivateKey) throws Exception {
//
//    String privateKeyPEM = base64PrivateKey.replace("-----BEGIN PRIVATE KEY-----", "")
//        .replace("-----END PRIVATE KEY-----", "")
//        .replaceAll("\\s+", "");
//    byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
//
//    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
//    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//    return keyFactory.generatePrivate(keySpec);
//  }
//
//  private void writeToFile(String content) throws IOException {
//    try (BufferedWriter writer = new BufferedWriter(new FileWriter(AppTest.INPUT_FILE_PATH))) {
//      writer.write(content);
//    }
//  }
//
//  private void cleanUpFiles() {
//    deleteFile(INPUT_FILE_PATH);
//    deleteFile(ENCRYPTED_FILE_PATH);
//    deleteFile(DECRYPTED_FILE_PATH);
//  }
//
//  private void deleteFile(String filePath) {
//    File file = new File(filePath);
//    if (file.exists()) {
//      file.delete();
//    }
//  }
//
//  @AfterEach
//  public void tearDown() {
//    cleanUpFiles();
//  }
//}
