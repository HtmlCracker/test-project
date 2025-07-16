package org.example.api.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class EncryptionUtilsTest {
    @InjectMocks
    EncryptionUtils encryptionUtils;

    @Test
    void encrypt_shouldReturnFileSize() throws Exception {
        File inputFile = createTempFile("test".getBytes());
        File outputFile = createTempFile(new byte[0]);

        long result = encryptionUtils.encrypt("validKey12345678", inputFile, outputFile);

        assertTrue(result > 0);
        inputFile.delete();
        outputFile.delete();
    }

    @Test
    void decrypt_shouldReturnFileSize() throws Exception {
        File inputFile = createTempFile("encryptedData".getBytes());
        File outputFile = createTempFile(new byte[0]);

        long result = encryptionUtils.decrypt("validKey12345678", inputFile, outputFile);

        assertTrue(result >= 0);
        inputFile.delete();
        outputFile.delete();
    }

    private File createTempFile(byte[] content) throws IOException {
        File file = File.createTempFile("test", ".tmp");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content);
        }
        return file;
    }
}
