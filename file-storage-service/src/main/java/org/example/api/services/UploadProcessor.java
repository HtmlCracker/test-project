package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.service.CompressedFileDto;
import org.example.api.dto.service.EncryptedFileDto;
import org.example.api.dto.service.StoredFileDto;
import org.example.api.entities.FileInfoEntity;
import org.example.api.entities.FolderEntity;
import org.example.api.repositories.FolderRepository;
import org.example.api.services.compression.CompressorService;
import org.example.api.services.encryption.EncryptorService;
import org.example.api.services.storage.PermanentStorageService;
import org.example.api.statemachine.enums.FileState;
import org.example.api.utils.FileUtils;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@WithStateMachine
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UploadProcessor {
    FileInfoCacheService fileInfoCacheService;
    FolderRepository folderRepository;
    CompressorService compressorService;
    EncryptorService encryptorService;
    PermanentStorageService permanentStorageService;
    FileUtils fileUtils;

    public void compress(String id) {
        UUID fileId = UUID.fromString(id);
        FileInfoEntity entity = fileInfoCacheService.getFileEntityById(fileId);
        String path = entity.getFilePath();

        CompressedFileDto dto = compressorService.compressFileAndWrite(path);
        String newPath = dto.getPath();
        deleteSourceAfterProcessing(path);
        updateFileInfoEntity(path, newPath, dto.getCompressedSize(), FileState.COMPRESSED);
    }

    public void encrypt(String id) {
        UUID fileId = UUID.fromString(id);
        FileInfoEntity entity = fileInfoCacheService.getFileEntityById(fileId);
        String path = entity.getFilePath();

        EncryptedFileDto dto = encryptorService.encryptFileAndWrite(path);
        String newPath = dto.getPath();
        deleteSourceAfterProcessing(path);
        updateFileInfoEntity(path, newPath, dto.getEncryptedSize(), FileState.ENCRYPTED);
    }

    public void store(String id) {
        UUID fileId = UUID.fromString(id);
        FileInfoEntity entity = fileInfoCacheService.getFileEntityById(fileId);
        String path = entity.getFilePath();

        StoredFileDto dto = permanentStorageService.permanentUploadFile(path);
        String newPath = dto.getPath();
        deleteSourceAfterProcessing(path);

        FileInfoEntity updatedFileInfoEntity = updateFileInfoEntity(
                path, newPath, dto.getFileSize(), dto.getFolderEntity(), FileState.STORED
        );
        bindFileToFolder(dto.getFolderEntity(), updatedFileInfoEntity);
    }

    private FileInfoEntity updateFileInfoEntity(String path,
                                                String newPath,
                                                Long newSize,
                                                FileState fileState) {
        FileInfoEntity fileInfoEntity = fileInfoCacheService.getFileEntityByPath(path);

        fileInfoEntity.setFilePath(newPath);
        fileInfoEntity.setCurrentSize(newSize);
        fileInfoEntity.setFileState(fileState);
        return fileInfoCacheService.saveFileInfoEntity(fileInfoEntity);
    }

    private FileInfoEntity updateFileInfoEntity(String path,
                                                String newPath,
                                                Long newSize,
                                                FolderEntity folderEntity,
                                                FileState fileState) {
        FileInfoEntity fileInfoEntity = fileInfoCacheService.getFileEntityByPath(path);

        fileInfoEntity.setFilePath(newPath);
        fileInfoEntity.setCurrentSize(newSize);
        fileInfoEntity.setFolder(folderEntity);
        fileInfoEntity.setFileState(fileState);
        return fileInfoCacheService.saveFileInfoEntity(fileInfoEntity);
    }

    private FolderEntity bindFileToFolder(FolderEntity folderEntity, FileInfoEntity fileInfoEntity) {
        int fileCount = folderEntity.getFileCount() + 1;
        folderEntity.setFileCount(fileCount);

        Long usedStorageByte = folderEntity.getUsedStorageByte() + fileInfoEntity.getCurrentSize();
        folderEntity.setUsedStorageByte(usedStorageByte);

        folderEntity.getFiles().add(fileInfoEntity);

        return saveFolderEntity(folderEntity);
    }

    private FolderEntity saveFolderEntity(FolderEntity folder) {
        return folderRepository.save(folder);
    }

    private void deleteSourceAfterProcessing(String path) {
        fileUtils.deleteFile(path);
    }
}
