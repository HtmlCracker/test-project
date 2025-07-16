package org.example.api.utils;

import org.example.api.exceptions.CryptoException;
import org.example.api.exceptions.FileProcessingException;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

@Component
public class EncryptionUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public long encrypt(String key, File inputFile, File outputFile) {
        return doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
    }

    public long decrypt(String key, File inputFile, File outputFile) {
        return doCrypto(Cipher.DECRYPT_MODE, key, inputFile, outputFile);
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

    private Cipher initCipher(int cipherMode, String key) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(TRANSFORMATION);
            Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            cipher.init(cipherMode, secretKey);
        } catch (Exception e) {
            throw new FileProcessingException("Cipher ERROR");
        }
        return cipher;
    }
}
