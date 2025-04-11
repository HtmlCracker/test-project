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
import java.io.InputStream;

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

    public EncryptedFileDto encryptFileAndWrite(String path) {
        File file = fileUtils.getFileOrThrowException(path);
        byte[] encryptedByte = encryptionUtils.encrypt(encryptionKey, file);
        String pathToCompressedFile = writeFile(encryptedByte, path);

        return EncryptedFileDto.builder()
                .path(pathToCompressedFile)
                .encryptedSize((long) encryptedByte.length)
                .build();
    }

    private String writeFile(byte[] compressedByte,
                             String oldPath) {
        String fileName = fileUtils.getFileName(oldPath);

        return fileUtils.createFileInDir(fileName, compressedByte, encryptedStoragePath);
    }
}
