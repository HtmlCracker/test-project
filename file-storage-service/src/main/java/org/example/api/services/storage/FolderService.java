package org.example.api.services.storage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.entities.FolderEntity;
import org.example.api.exceptions.BadRequestException;
import org.example.api.exceptions.FileProcessingException;
import org.example.api.repositories.FolderRepository;
import org.example.api.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FolderService {
    @Value("${maxFileCountInFolder}")
    int maxFileCount;
    final FileUtils fileUtils;
    final FolderRepository folderRepository;

    public FolderEntity createRootIfNotExists(String storagePath) {
        FolderEntity root = folderRepository.findByFolderName("root");

        if (root == null) {
            String path = storagePath + "/root";
            root = FolderEntity.builder()
                    .folderName("root")
                    .path(path)
                    .build();
            fileUtils.createDirectoryIfNotExists(path);
            System.out.println("Root: " + path);
            return saveFolderEntity(root);
        }

        return root;
    }

    public FolderEntity getLeastFilledFolder() {
        FolderEntity folderEntity = folderRepository.findWithMinFileCount()
                .orElseThrow(() -> new BadRequestException("Something went wrong.")).get(0);

        if (folderEntity.getFileCount() < maxFileCount)
            return folderEntity;

        ArrayList<FolderEntity> newFolders = createAndAddNodes();
        return newFolders.get(0);
    }

    private ArrayList<FolderEntity> createAndAddNodes() {
        ArrayList<FolderEntity> lastCreatedFolders = folderRepository.findByIsLast(true)
                .orElseThrow(() -> new FileProcessingException("Something went wrong."));

        ArrayList<FolderEntity> newFolders = new ArrayList<>();

        for (FolderEntity folder : lastCreatedFolders) {
            newFolders.addAll(createNewFolder(folder));
        }

        return newFolders;
    }
    private List<FolderEntity> createNewFolder(FolderEntity parentFolder) {
        Random random = new Random();
        List<FolderEntity> savedFolders = new ArrayList<>();

        parentFolder.setIsLast(false);

        Path parentPath = Path.of(parentFolder.getPath());

        for (byte i = 0; i < 2; i++) {
            String folderName;
            if (i == 0) {
                folderName = String.format("A%d", random.nextInt(10));
            } else {
                folderName = String.format("B%d", random.nextInt(10));
            }

            fileUtils.createDirectoryIfNotExists(parentPath + "/" + folderName);
            FolderEntity newFolder = makeFolderEntity(folderName, parentFolder, parentPath.resolve(folderName));

            FolderEntity savedFolder = saveFolderEntity(newFolder);
            parentFolder.getChildrens().add(savedFolder);

            savedFolders.add(savedFolder);
        }
        saveFolderEntity(parentFolder);

        return savedFolders;
    }

    private FolderEntity makeFolderEntity(String folderName, FolderEntity parent, Path folderPath) {
        return FolderEntity.builder()
                .folderName(folderName)
                .parent(parent)
                .path(folderPath.toString())
                .build();
    }

    private FolderEntity saveFolderEntity(FolderEntity entity) {
        return folderRepository.save(entity);
    }
}
