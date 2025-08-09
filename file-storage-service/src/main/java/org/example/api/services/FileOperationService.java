package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.example.api.dto.response.DeleteFileResponseDto;
import org.example.api.dto.service.CompressedFileDto;
import org.example.api.dto.service.EncryptedFileDto;
import org.example.api.dto.service.StoredFileDto;
import org.example.api.entities.FileInfoEntity;
import org.example.api.entities.FolderEntity;
import org.example.api.exceptions.NotFoundException;
import org.example.api.repositories.FileInfoRepository;
import org.example.api.repositories.FolderRepository;
import org.example.api.services.compression.CompressorService;
import org.example.api.services.encryption.EncryptorService;
import org.example.api.services.storage.PermanentStorageService;
import org.example.api.statemachine.state.upload.UploadFileState;
import org.example.api.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@WithStateMachine
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileOperationService {
    FileInfoCacheService fileInfoCacheService;
    FileInfoRepository fileInfoRepository;
    FolderRepository folderRepository;
    CompressorService compressorService;
    EncryptorService encryptorService;
    PermanentStorageService permanentStorageService;

    @NonFinal
    @Value("${PATH_TO_PREPARED_FOR_GET_STORAGE}")
    private String preparedForGetStoragePath;

    @NonFinal
    @Value("${PATH_TO_READY_FOR_GET_STORAGE}")
    private String pathToReadyForGetStorage;

    FileUtils fileUtils;

    @Transactional
    public String getFileMimeType(String fileId) {
        UUID uuid = UUID.fromString(fileId);
        return fileInfoCacheService.getFileEntityById(uuid).getMimeType();
    }

    @Transactional
    public void compress(String id) {
        UUID fileId = UUID.fromString(id);
        FileInfoEntity entity = fileInfoCacheService.getFileEntityById(fileId);
        String path = entity.getFilePath();

        CompressedFileDto dto = compressorService.compressFileAndWrite(path);
        String newPath = dto.getPath();
        deleteSourceAfterProcessing(path);
        updateFileInfoEntity(path, newPath, dto.getCompressedSize(), UploadFileState.COMPRESSED);
    }

    @Transactional
    public void encrypt(String id) {
        UUID fileId = UUID.fromString(id);
        FileInfoEntity entity = fileInfoCacheService.getFileEntityById(fileId);
        String path = entity.getFilePath();

        EncryptedFileDto dto = encryptorService.encryptFileAndWrite(path);
        String newPath = dto.getPath();
        deleteSourceAfterProcessing(path);
        updateFileInfoEntity(path, newPath, dto.getEncryptionKey(), dto.getEncryptedSize(), UploadFileState.ENCRYPTED);
    }

    @Transactional
    public void store(String id) {
        UUID fileId = UUID.fromString(id);
        FileInfoEntity entity = fileInfoCacheService.getFileEntityById(fileId);
        String path = entity.getFilePath();

        StoredFileDto dto = permanentStorageService.permanentUploadFile(path);
        String newPath = dto.getPath();
        deleteSourceAfterProcessing(path);

        FileInfoEntity updatedFileInfoEntity = updateFileInfoEntity(
                path, newPath, dto.getFileSize(), dto.getFolderEntity(), UploadFileState.STORED
        );
        bindFileToFolder(dto.getFolderEntity(), updatedFileInfoEntity);
    }

    public DeleteFileResponseDto delete(UUID id) {
        FileInfoEntity entity = fileInfoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Entity with id %s if not exists", id))
                );

        fileUtils.deleteFile(entity.getFilePath());
        fileInfoCacheService.deleteFile(entity);

        return DeleteFileResponseDto.builder()
                .status("deleted")
                .build();
    }

    public String prepare(String path) {
        String fileName = fileUtils.getFileName(path);
        return fileUtils.moveFileTo(path, preparedForGetStoragePath + "/" + fileName);
    }

    public String decrypt(String path, String encryptionKey) {
        String newPath = encryptorService.decryptFileAndWrite(path, encryptionKey);

        deleteSourceAfterProcessing(path);
        return newPath;
    }

    public String decompress(String path) {
        String newPath = compressorService.decompressFileAndWrite(path);

        deleteSourceAfterProcessing(path);
        return newPath;
    }

    public String deliver(String path) {
        String fileName = fileUtils.getFileName(path);
        String newPath = fileUtils.moveFileTo(path, pathToReadyForGetStorage + "/" + fileName);

        deleteSourceAfterProcessing(path);
        return newPath;
    }

    private FileInfoEntity updateFileInfoEntity(String path,
                                                String newPath,
                                                Long newSize,
                                                UploadFileState fileState) {
        FileInfoEntity fileInfoEntity = fileInfoCacheService.getFileEntityByPath(path);

        fileInfoEntity.setFilePath(newPath);
        fileInfoEntity.setCurrentSize(newSize);
        fileInfoEntity.setFileState(fileState);
        return fileInfoCacheService.saveFileInfoEntity(fileInfoEntity);
    }

    private FileInfoEntity updateFileInfoEntity(String path,
                                                String newPath,
                                                String encryptionKey,
                                                Long encryptedSize,
                                                UploadFileState fileState) {
        FileInfoEntity fileInfoEntity = fileInfoCacheService.getFileEntityByPath(path);

        fileInfoEntity.setFilePath(newPath);
        fileInfoEntity.setEncryptionKey(encryptionKey);
        fileInfoEntity.setCurrentSize(encryptedSize);
        fileInfoEntity.setFileState(fileState);
        return fileInfoCacheService.saveFileInfoEntity(fileInfoEntity);
    }

    private FileInfoEntity updateFileInfoEntity(String path,
                                                String newPath,
                                                Long newSize,
                                                FolderEntity folderEntity,
                                                UploadFileState fileState) {
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
