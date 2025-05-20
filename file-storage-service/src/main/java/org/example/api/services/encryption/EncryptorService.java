package org.example.api.services.encryption;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.service.EncryptedFileDto;
import org.example.api.utils.EncryptionUtils;
import org.example.api.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class EncryptorService {
    final FileUtils fileUtils;
    final EncryptionUtils encryptionUtils;

    @Value("${ENCRYPTION_KEY}")
    String encryptionKey;

    @Value("${PATH_TO_ENCRYPTED_STORAGE}")
    String encryptedStoragePath;

    @Value("${PATH_TO_DECRYPTED_STORAGE}")
    private String decryptedStoragePath;

    public EncryptedFileDto encryptFileAndWrite(String path) {
        File file = fileUtils.getFileOrThrowException(path);
        byte[] encryptedByte = encryptionUtils.encrypt(encryptionKey, file);
        String pathToEncryptedFile = writeEncryptedFile(encryptedByte, path);

        return EncryptedFileDto.builder()
                .path(pathToEncryptedFile)
                .encryptedSize((long) encryptedByte.length)
                .build();
    }

    public String decryptFileAndWrite(String path) {
        File file = fileUtils.getFileOrThrowException(path);
        byte[] decryptedByte = encryptionUtils.decrypt(encryptionKey, file);
        System.out.println(decryptedByte.length);
        return writeDecryptedFile(decryptedByte, path);
    }

    private String writeEncryptedFile(byte[] compressedByte,
                             String oldPath) {
        String fileName = fileUtils.getFileName(oldPath);
        return fileUtils.createFileInDir(fileName, compressedByte, encryptedStoragePath);
    }

    private String writeDecryptedFile(byte[] compressedByte,
                                      String oldPath) {
        String fileName = fileUtils.getFileName(oldPath);
        return fileUtils.createFileInDir(fileName, compressedByte, decryptedStoragePath);
    }
}
