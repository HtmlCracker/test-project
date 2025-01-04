package org.example.api.services;

import org.example.api.dto.response.StorageDto;
import org.example.api.entities.StorageEntity;
import org.example.api.exceptions.BadRequestException;
import org.example.api.factories.StorageDtoFactory;
import org.example.api.repositories.StorageRepository;
import org.example.api.utils.FileUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StorageService {
    StorageRepository storageRepository;
    FileUtils fileUtils = FileUtils.of("/app/storage-files");
    StorageDtoFactory storageDtoFactory;

    public StorageDto createStorage(UUID profileId) {
        StorageEntity savedEntity = storageRepository.save(storageEntityBuilder(profileId));

        try {
            fileUtils.createDirectory(savedEntity.getId().toString());
        } catch (BadRequestException e) {
            storageRepository.delete(savedEntity);
        }

        return storageDtoFactory.makeStorageDto(savedEntity);
    }

    private StorageEntity storageEntityBuilder(UUID profileId) {
        return StorageEntity.builder()
                .id(profileId)
                .build();
    }
}
