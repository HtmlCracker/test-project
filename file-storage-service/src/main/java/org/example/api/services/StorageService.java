package org.example.api.services;


import org.example.api.dto.response.StorageDto;
import org.example.api.dto.response.UploadFileResponseDto;
import org.example.api.entities.FileInfoEntity;
import org.example.api.entities.StorageEntity;
import org.example.api.exceptions.BadRequestException;
import org.example.api.exceptions.IOException;
import org.example.api.exceptions.NotFoundException;
import org.example.api.factories.StorageDtoFactory;
import org.example.api.factories.UploadFileResponseDtoFactory;
import org.example.api.repositories.FileInfoRepository;
import org.example.api.repositories.StorageRepository;
import org.example.api.utils.FileUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StorageService {
    StorageRepository storageRepository;
    FileInfoRepository fileInfoRepository;

    FileUtils fileUtils = FileUtils.of("/app/storage-files");

    StorageDtoFactory storageDtoFactory;
    UploadFileResponseDtoFactory uploadFileResponseDtoFactory;

    public StorageDto createStorage(String profileId) {
        UUID profileIdUuid = UUID.fromString(profileId);

        storageRepository.findByOwnerId(profileIdUuid).ifPresent((storage -> {
                    throw new BadRequestException(String.format("Storage for profile id %s already exists", profileId));
                }));

        StorageEntity savedEntity = saveStorageEntity(storageEntityBuilder(profileIdUuid));

        try {
            fileUtils.createDirectory(savedEntity.getId().toString());
        } catch (BadRequestException e) {
            storageRepository.delete(savedEntity);
        }

        return storageDtoFactory.makeStorageDto(savedEntity);
    }

    private StorageEntity storageEntityBuilder(UUID profileId) {
        return StorageEntity.builder()
                .ownerId(profileId)
                .build();
    }

    private StorageEntity saveStorageEntity(StorageEntity storage) {
        return storageRepository.save(storage);
    }


    public UploadFileResponseDto uploadFile(String ownerId, MultipartFile file) {
        UUID ownerIdUuid = UUID.fromString(ownerId);

        if (file.isEmpty())
            throw new BadRequestException("File can't be empty.");

        StorageEntity storage = storageRepository.findByOwnerId(ownerIdUuid)
                .orElseThrow(() -> new NotFoundException(String.format("Storage for profile id \"%s\" does not exist", ownerIdUuid)));

        FileInfoEntity fileInfoEntity = makeFileInfoEntity(storage, file);
        FileInfoEntity savedFileInfoEntity = saveFileInfoEntity(fileInfoEntity);

        StorageEntity updatedStorage = saveStorageEntity(bindStorageAndFile(storage, savedFileInfoEntity));

        try {
            saveFileToStorage(fileInfoEntity, file);
        } catch (IOException e) {
            updatedStorage.getFiles().remove(savedFileInfoEntity);
            saveStorageEntity(updatedStorage);
            deleteFileInfoEntity(savedFileInfoEntity);
        }

        return uploadFileResponseDtoFactory.makeUploadFileResponseDto(savedFileInfoEntity);
    }

    private FileInfoEntity makeFileInfoEntity(StorageEntity storage, MultipartFile file) {
        return FileInfoEntity.builder()
                .originalName(file.getOriginalFilename())
                .fileSizeByte(file.getSize())
                .storage(storage)
                .build();
    }

    private FileInfoEntity saveFileInfoEntity(FileInfoEntity entity) {
        return fileInfoRepository.save(entity);
    }

    private void deleteFileInfoEntity(FileInfoEntity entity) {
        fileInfoRepository.delete(entity);
    }

    private StorageEntity bindStorageAndFile(StorageEntity storage, FileInfoEntity fileInfoEntity) {
        storage.getFiles().add(fileInfoEntity);
        return storage;
    }

    private void saveFileToStorage(FileInfoEntity fileInfoEntity, MultipartFile file) {
        String path = fileInfoEntity.getStorage().getId().toString();
        System.out.println(path);
        String fileName = fileInfoEntity.getId().toString();

        fileUtils.addFileToPath(path, fileName, file);
    }
}
