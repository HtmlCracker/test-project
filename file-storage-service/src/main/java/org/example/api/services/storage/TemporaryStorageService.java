package org.example.api.services.storage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.entities.FileInfoEntity;
import org.example.api.services.FileInfoCacheService;
import org.example.api.statemachine.state.upload.UploadFileState;
import org.example.api.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TemporaryStorageService {
    final FileUtils fileUtils;
    final FileInfoCacheService fileInfoCacheService;

    @Value("${PATH_TO_TEMPORARY_STORAGE}")
    private String temporaryStoragePath;

    public FileInfoEntity temporaryUploadFile(MultipartFile file) {
        String fileHash = fileUtils.calculateUniqueFileHash(file);
        String fileName = fileHash + "." + fileUtils.getFileExtension(file.getOriginalFilename());
        String filePath = fileUtils.createFileInDir(fileName, file, temporaryStoragePath);
        FileInfoEntity fileInfoEntity = constructNewEntity(file, fileHash, filePath);

        return fileInfoCacheService.saveFileInfoEntity(fileInfoEntity);
    }

    private FileInfoEntity constructNewEntity(MultipartFile file,
                                              String fileHash,
                                              String filePath) {
        String origFileName = file.getOriginalFilename();
        String fileExt = fileUtils.getFileExtension(origFileName);

        return FileInfoEntity.builder()
                .originalFileName(origFileName)
                .originalFileSize(file.getSize())
                .fileExt(fileExt)
                .fileState(UploadFileState.UPLOADED)
                .fileHash(fileHash)
                .filePath(filePath)
                .build();
    }
}
