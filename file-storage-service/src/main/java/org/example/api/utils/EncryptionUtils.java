package org.example.api.utils;

import org.example.api.exceptions.FileProcessingException;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class EncryptionUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static final int KEY_SIZE = 256;

    public long encrypt(String base64Key, File inputFile, File outputFile) {
        validateKey(base64Key);
        return doCrypto(Cipher.ENCRYPT_MODE, base64Key, inputFile, outputFile);
    }

    public long decrypt(String base64Key, File inputFile, File outputFile) {
        validateKey(base64Key);
        return doCrypto(Cipher.DECRYPT_MODE, base64Key, inputFile, outputFile);
    }

    private long doCrypto(int cipherMode, String key, File inputFile, File outputFile) {
        try (FileInputStream inputStream = new FileInputStream(inputFile);
                FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            Cipher cipher = initCipher(cipherMode, key);

            try (CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
                byte[] buffer = new byte[4 * 1024 * 1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    cipherOutputStream.write(buffer, 0, bytesRead);
                }
            }
            return outputFile.length();
        } catch (IOException | SecurityException e) {
            throw new FileProcessingException("File processing error");
        }
    }

    public String generateAES256Key() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate AES key", e);
        }
    }

    private Cipher initCipher(int cipherMode, String base64Key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(cipherMode, secretKey);
            return cipher;
        } catch (Exception e) {
            throw new FileProcessingException("Cipher initialization error");
        }
    }

    private void validateKey(String base64Key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            if (keyBytes.length != KEY_SIZE / 8) {
                throw new IllegalArgumentException(
                        "Invalid key length. Expected " + (KEY_SIZE/8) +
                                " bytes for AES-" + KEY_SIZE);
            }
        } catch (IllegalArgumentException e) {
            throw new FileProcessingException("Invalid Base64 key format");
        }
    }
}
