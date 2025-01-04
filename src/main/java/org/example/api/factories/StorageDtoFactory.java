package org.example.api.factories;

import org.example.api.dto.response.StorageDto;
import org.example.api.entities.StorageEntity;
import org.springframework.stereotype.Component;

@Component
public class StorageDtoFactory {
    public StorageDto makeStorageDto(StorageEntity entity) {
        return StorageDto.builder()
                .id(entity.getId())
                .usedStorageMB(entity.getUsedStorageMB())
                .build();
    }
}
