package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.entities.FileInfoEntity;
import org.example.api.enums.FileStates;
import org.example.api.repositories.FileInfoRepository;
import org.example.api.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class StorageService {
    final FileInfoRepository fileInfoRepository;
    final FileUtils fileUtils;

    @Value("${PATH_TO_TEMPORARY_STORAGE}")
    private String temporaryStoragePath;

    public FileInfoEntity temporaryUploadFile(MultipartFile file) {
        String fileHash = fileUtils.calculateFileHash(file);
        String filePath = fileUtils.createFileInDir(fileHash, file, temporaryStoragePath);
        FileInfoEntity fileInfoEntity = constructNewEntity(file, fileHash, filePath);

        return saveFileInfoEntity(fileInfoEntity);
    }

    private FileInfoEntity constructNewEntity(MultipartFile file,
                                              String fileHash,
                                              String filePath) {
        String origFileName = file.getOriginalFilename();
        return FileInfoEntity.builder()
                .originalFileName(origFileName)
                .originalFileSize(file.getSize())
                .fileState(FileStates.UPLOADED)
                .fileHash(fileHash)
                .filePath(filePath)
                .build();
    }

    private FileInfoEntity saveFileInfoEntity(FileInfoEntity fileInfoEntity) {
        return fileInfoRepository.save(fileInfoEntity);
    }
}
