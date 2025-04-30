package org.example.api.configs;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.api.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class RunAfterStartup {
    @Autowired
    FileUtils fileUtils;

    @Value("${PATH_TO_TEMPORARY_STORAGE}")
    private String temporaryStoragePath;

    @Value("${PATH_TO_COMPRESSED_STORAGE}")
    private String compressedStoragePath;

    @Value("${PATH_TO_ENCRYPTED_STORAGE}")
    private String encryptedStoragePath;

    @Value("${PATH_TO_PERMANENT_STORAGE}")
    private String permanentStoragePath;

    @Value("${PATH_TO_PREPARED_FOR_GET_STORAGE}")
    private String preparedForGetStoragePath;

    @Value("${PATH_TO_DECRYPTED_STORAGE}")
    private String decryptedStorage;

    @Value("${PATH_TO_DECOMPRESSED_STORAGE}")
    private String decompressedStorage;

    @Value("${PATH_TO_READY_FOR_GET_STORAGE}")
    private String readyForGetStoragePath;

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        fileUtils.createDirectoryIfNotExists(temporaryStoragePath);
        fileUtils.createDirectoryIfNotExists(compressedStoragePath);
        fileUtils.createDirectoryIfNotExists(encryptedStoragePath);
        fileUtils.createDirectoryIfNotExists(permanentStoragePath);

        fileUtils.createDirectoryIfNotExists(preparedForGetStoragePath);
        fileUtils.createDirectoryIfNotExists(decryptedStorage);
        fileUtils.createDirectoryIfNotExists(decompressedStorage);
        fileUtils.createDirectoryIfNotExists(readyForGetStoragePath);

        System.out.println("CREATED");
    }
}