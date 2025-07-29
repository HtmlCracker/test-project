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

    @Value("${encryptionKey}")
    String encryptionKey;

    @Value("${PATH_TO_ENCRYPTED_STORAGE}")
    String encryptedStoragePath;

    @Value("${PATH_TO_DECRYPTED_STORAGE}")
    private String decryptedStoragePath;

    public EncryptedFileDto encryptFileAndWrite(String path) {
        File file = fileUtils.getFileOrThrowException(path);
        String fileName = fileUtils.getFileName(path);
        File encryptedFile = new File(encryptedStoragePath, fileName);

        long size = encryptionUtils.encrypt(encryptionKey, file, encryptedFile);

        return EncryptedFileDto.builder()
                .path(encryptedFile.getPath())
                .encryptedSize(size)
                .build();
    }

    public String decryptFileAndWrite(String path) {
        File file = fileUtils.getFileOrThrowException(path);
        String fileName = fileUtils.getFileName(path);
        File encryptedFile = new File(decryptedStoragePath, fileName);
        long size = encryptionUtils.decrypt(encryptionKey, file, encryptedFile);

        return encryptedFile.getPath();
    }
}
