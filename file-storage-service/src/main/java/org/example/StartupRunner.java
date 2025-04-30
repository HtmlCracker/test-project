package org.example;

import org.example.api.services.storage.FolderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {
    @Value("${PATH_TO_PERMANENT_STORAGE}")
    String storagePath;
    private final FolderService folderService;
    public StartupRunner(FolderService folderService) {
        this.folderService = folderService;
    }

    @Override
    public void run(String... args) {
        folderService.createRootIfNotExists(storagePath);
    }
}
