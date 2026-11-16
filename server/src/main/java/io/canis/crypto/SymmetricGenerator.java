package io.canis.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class SymmetricGenerator {

    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final byte[] GCM_MAGIC = "CANISGCM1".getBytes(StandardCharsets.US_ASCII);
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SecretKey secretKey;

    public SymmetricGenerator(String secretKeyPath) {
        this.secretKey = MasterKeyLoader.load(secretKeyPath);
    }

    public boolean matchesKeyFile(String secretKeyPath) {
        SecretKey candidate = MasterKeyLoader.load(secretKeyPath);
        byte[] loaded = secretKey.getEncoded();
        byte[] configured = candidate.getEncoded();
        try {
            return MessageDigest.isEqual(loaded, configured);
        } finally {
            Arrays.fill(loaded, (byte) 0);
            Arrays.fill(configured, (byte) 0);
        }
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
}
