package org.example.api.utils;

import org.example.api.exceptions.CryptoException;
import org.example.api.exceptions.FileProcessingException;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

@Component
public class EncryptionUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public byte[] encrypt(String key, File inputFile)
            throws CryptoException {
        return doCrypto(Cipher.ENCRYPT_MODE, key, inputFile);
    }

    public byte[] decrypt(String key, File inputFile)
            throws CryptoException {
        return doCrypto(Cipher.DECRYPT_MODE, key, inputFile);
    }

    private byte[] doCrypto(int cipherMode, String key, File inputFile) {
        byte[] inputBytes;

        try (FileInputStream inputStream = new FileInputStream(inputFile);) {
            inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);
        } catch (IOException e) {
            throw new FileProcessingException("File stream exception");
        }

        return cryptoInputBytes(cipherMode, key, inputBytes);
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

}
