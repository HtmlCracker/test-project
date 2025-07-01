package org.example.api.services.storage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.service.StoredFileDto;
import org.example.api.entities.FolderEntity;
import org.example.api.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PermanentStorageService {
    @Value("${PATH_TO_PERMANENT_STORAGE}")
    private String permanentStoragePath;
    final FolderService folderService;
    final FileUtils fileUtils;

    public StoredFileDto permanentUploadFile(String path) {
        FolderEntity folder = folderService.getLeastFilledFolder();
        String folderPath = folder.getPath();

        String fileName = fileUtils.getFileName(path);
        File file = fileUtils.getFileOrThrowException(path);
        String storedPath = fileUtils.createFileInDir(fileName, file, folderPath);

        return StoredFileDto.builder()
                .path(storedPath)
                .fileSize(file.length())
                .folderEntity(folder)
                .build();
    }
}
