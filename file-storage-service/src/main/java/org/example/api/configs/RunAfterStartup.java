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

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        fileUtils.createDirectoryIfNotExists(temporaryStoragePath);
        fileUtils.createDirectoryIfNotExists(compressedStoragePath);
        fileUtils.createDirectoryIfNotExists(encryptedStoragePath);
        fileUtils.createDirectoryIfNotExists(permanentStoragePath);
    }
}
