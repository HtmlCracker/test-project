package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
import org.example.api.utils.FileUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UploadProcessor {
    FileInfoRepository fileInfoRepository;
    FolderRepository folderRepository;
    CompressorService compressorService;
    EncryptorService encryptorService;
    PermanentStorageService permanentStorageService;
    FileUtils fileUtils;

    public FileInfoEntity compress(String path) {
        CompressedFileDto dto = compressorService.compressFileAndWrite(path);
        String newPath = dto.getPath();
        deleteSourceAfterProcessing(path);

        return updateFileInfoEntity(path, newPath, dto.getCompressedSize());
    }

    public FileInfoEntity encrypt(String path) {
        EncryptedFileDto dto = encryptorService.encryptFileAndWrite(path);
        String newPath = dto.getPath();
        deleteSourceAfterProcessing(path);

        return updateFileInfoEntity(path, newPath, dto.getEncryptedSize());
    }

    public FileInfoEntity store(String path) {
        StoredFileDto dto = permanentStorageService.permanentUploadFile(path);
        String newPath = dto.getPath();
        deleteSourceAfterProcessing(path);

        FileInfoEntity updatedFileInfoEntity = updateFileInfoEntity(path, newPath, dto.getFileSize(), dto.getFolderEntity());
        bindFileToFolder(dto.getFolderEntity(), updatedFileInfoEntity);

        return updatedFileInfoEntity;
    }

    private FileInfoEntity updateFileInfoEntity(String oldPath,
                                                String newPath,
                                                Long newSize) {
        FileInfoEntity fileInfoEntity = fileInfoRepository.findByFilePath(oldPath)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Entity with path: %s is not exists.", oldPath)
                ));
        fileInfoEntity.setFilePath(newPath);
        fileInfoEntity.setCurrentSize(newSize);
        return fileInfoRepository.save(fileInfoEntity);
    }

    private FileInfoEntity updateFileInfoEntity(String oldPath,
                                                String newPath,
                                                Long newSize,
                                                FolderEntity folderEntity) {
        FileInfoEntity fileInfoEntity = fileInfoRepository.findByFilePath(oldPath)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Entity with path: %s is not exists.", oldPath)
                ));
        fileInfoEntity.setFilePath(newPath);
        fileInfoEntity.setCurrentSize(newSize);
        fileInfoEntity.setFolder(folderEntity);
        return fileInfoRepository.save(fileInfoEntity);
    }

    private FolderEntity bindFileToFolder(FolderEntity folderEntity, FileInfoEntity fileInfoEntity) {
        int fileCount = folderEntity.getFileCount()+1;
        folderEntity.setFileCount(fileCount);

        Long usedStorageByte = folderEntity.getUsedStorageByte() + fileInfoEntity.getCurrentSize();
        folderEntity.setUsedStorageByte(usedStorageByte);

        folderEntity.getFiles().add(fileInfoEntity);

        return save(folderEntity);
    }

    private FolderEntity save(FolderEntity folder) {
        return folderRepository.save(folder);
    }

    private void deleteSourceAfterProcessing(String path) {
        fileUtils.deleteFile(path);
    }
}
