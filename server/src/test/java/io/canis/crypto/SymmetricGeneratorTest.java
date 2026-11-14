package io.canis.crypto;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class SymmetricGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void testEncryptUsesRandomNonceAndDecrypts() throws Exception {
        Path secretKeyPath = tempDir.resolve("secret.key");
        SymmetricGenerator generator = new SymmetricGenerator(secretKeyPath.toString());
        byte[] plaintext = "sensitive database payload".getBytes(StandardCharsets.UTF_8);

        byte[] firstCiphertext = generator.encrypt(plaintext);
        byte[] secondCiphertext = generator.encrypt(plaintext);

        assertFalse(Arrays.equals(firstCiphertext, secondCiphertext));
        assertArrayEquals(plaintext, generator.decrypt(firstCiphertext));
        assertArrayEquals(plaintext, new SymmetricGenerator(secretKeyPath.toString()).decrypt(secondCiphertext));
    }

    @Test
    void testDoesNotGenerateMissingKeyWhenGenerationIsDisabled() {
        Path secretKeyPath = tempDir.resolve("missing.key");

        assertThrows(java.io.IOException.class, () -> new SymmetricGenerator(secretKeyPath.toString(), false));
        assertFalse(java.nio.file.Files.exists(secretKeyPath));
    }
}
