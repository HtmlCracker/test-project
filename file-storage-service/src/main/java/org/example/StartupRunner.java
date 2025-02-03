package org.example;

import org.example.api.services.FolderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private final FolderService folderService;

    public StartupRunner(FolderService folderService) {
        this.folderService = folderService;
    }

    @Override
    public void run(String... args) throws Exception {
        folderService.createRootIfNotExists();
    }
}