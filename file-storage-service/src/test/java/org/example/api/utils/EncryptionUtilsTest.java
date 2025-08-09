package org.example.api.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EncryptionUtilsTest {
    @InjectMocks
    EncryptionUtils encryptionUtils;

    @Test
    void encrypt_shouldReturnFileSize() throws Exception {
        File inputFile = createTempFile("test".getBytes());
        File outputFile = createTempFile(new byte[0]);
        String validBase64Key = "YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWE=";

        long result = encryptionUtils.encrypt(validBase64Key, inputFile, outputFile);

        assertTrue(result > 0);
        inputFile.delete();
        outputFile.delete();
    }

    @Test
    void decrypt_shouldReturnFileSize() throws Exception {
        File inputFile = createTempFile("encryptedData".getBytes());
        File outputFile = createTempFile(new byte[0]);
        String validBase64Key = "YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWE=";

        long result = encryptionUtils.decrypt(validBase64Key, inputFile, outputFile);

        assertTrue(result >= 0);
        inputFile.delete();
        outputFile.delete();
    }

    @Test
    void generateAES256Key_shouldReturnValidBase64Key() {
        String key = encryptionUtils.generateAES256Key();

        assertNotNull(key);
        assertFalse(key.isEmpty());
        assertDoesNotThrow(() -> Base64.getDecoder().decode(key));

        byte[] decodedKey = Base64.getDecoder().decode(key);
        assertEquals(32, decodedKey.length);

        String key1 = encryptionUtils.generateAES256Key();
        String key2 = encryptionUtils.generateAES256Key();

        assertNotEquals(key1, key2);
    }

    private File createTempFile(byte[] content) throws IOException {
        File file = File.createTempFile("test", ".tmp");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content);
        }
        return file;
    }
}
