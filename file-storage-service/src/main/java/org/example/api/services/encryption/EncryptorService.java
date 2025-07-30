package org.example.api.services.encryption;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.service.EncryptedFileDto;
import org.example.api.services.VaultTransitService;
import org.example.api.utils.EncryptionUtils;
import org.example.api.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import java.io.File;
import java.security.NoSuchAlgorithmException;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class EncryptorService {
    final FileUtils fileUtils;
    final EncryptionUtils encryptionUtils;
    final VaultTransitService vaultTransitService;

    @Value("${PATH_TO_ENCRYPTED_STORAGE}")
    String encryptedStoragePath;

    @Value("${PATH_TO_DECRYPTED_STORAGE}")
    private String decryptedStoragePath;

    public EncryptedFileDto encryptFileAndWrite(String path) {
        File file = fileUtils.getFileOrThrowException(path);
        String fileName = fileUtils.getFileName(path);
        File encryptedFile = new File(encryptedStoragePath, fileName);
        String base64Key = encryptionUtils.generateAES256Key();

        long size = encryptionUtils.encrypt(base64Key, file, encryptedFile);

        String encryptedKey = vaultTransitService.encrypt(base64Key);

        return EncryptedFileDto.builder()
                .path(encryptedFile.getPath())
                .encryptionKey(encryptedKey)
                .encryptedSize(size)
                .build();
    }

    public String decryptFileAndWrite(String path, String encryptionKey) {
        File file = fileUtils.getFileOrThrowException(path);
        String fileName = fileUtils.getFileName(path);
        File encryptedFile = new File(decryptedStoragePath, fileName);
        String decryptedKey = vaultTransitService.decrypt(encryptionKey);

        long size = encryptionUtils.decrypt(decryptedKey, file, encryptedFile);

        return encryptedFile.getPath();
    }
}
