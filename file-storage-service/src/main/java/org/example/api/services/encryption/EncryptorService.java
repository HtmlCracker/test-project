package org.example.api.services.encryption;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

    public String encryptFileAndWrite(String path) {
        File file = fileUtils.getFileOrThrowException(path);
            byte[] encryptedBytes = encryptionUtils.encrypt(encryptionKey, file);

        System.out.println(encryptedBytes.toString());
        return encryptedBytes.toString();
    }
}
