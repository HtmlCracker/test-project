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

    public long encrypt(String key, File inputFile, File outputFile)
            throws CryptoException {
        return doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
    }

    public long decrypt(String key, File inputFile, File outputFile)
            throws CryptoException {
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

    private byte[] cryptoInputBytes(int cipherMode, String key, byte[] inputBytes) {
        Cipher cipher;
        Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
        byte[] outputBytes;

        try {
            cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(cipherMode, secretKey);
            outputBytes = cipher.doFinal(inputBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                IllegalBlockSizeException | BadPaddingException e) {
            throw new CryptoException("Crypto algorithm error");
        } catch (InvalidKeyException e) {
            throw new CryptoException("Invalid key exception");
        }
        return outputBytes;
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
