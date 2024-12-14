package io.canis.utils;

import java.io.FileReader;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class AsymmetricGenerator {

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        java.security.KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    public static String publicKeyToString(PublicKey publicKey) {       
        byte[] encodedKey = publicKey.getEncoded();
        return Base64.getEncoder().encodeToString(encodedKey);
    }

    public static String privateKeyToString(PrivateKey privateKey) {
        byte[] encodedKey = privateKey.getEncoded();
        return Base64.getEncoder().encodeToString(encodedKey);
    }

    public static PublicKey stringToPublicKey(String publicKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory  = KeyFactory.getInstance("RSA");
        return keyFactory .generatePublic(spec);
    }

    public static PrivateKey stringToPrivateKey(String privateKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    public static PublicKey loadPublicKey(String filePath) throws Exception {
       
        StringBuilder publicKeyString = new StringBuilder();
        try (Reader reader = new FileReader(filePath)) {
            int ch;
            while ((ch = reader.read()) != -1) {
                publicKeyString.append((char) ch);
            }
        }

        byte[] decodedKey = Base64.getDecoder().decode(publicKeyString.toString());

        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory  = KeyFactory.getInstance("RSA");
        return keyFactory .generatePublic(spec);
    }

    public static PrivateKey loadPrivateKey(String filePath) throws Exception {
        StringBuilder privateKeyString = new StringBuilder();
        try (Reader reader = new FileReader(filePath)) {
            int ch;
            while ((ch = reader.read()) != -1) {
                privateKeyString.append((char) ch);
            }
        }
        byte[] decodedKey = Base64.getDecoder().decode(privateKeyString.toString());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

}
