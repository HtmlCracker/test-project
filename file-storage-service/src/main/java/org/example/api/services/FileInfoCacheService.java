package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.entities.FileInfoEntity;
import org.example.api.exceptions.NotFoundException;
import org.example.api.repositories.FileInfoRepository;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class FileInfoCacheService {
    FileInfoRepository fileInfoRepository;

    @Caching(
            put = {
                    @CachePut(value = "fileInfoCacheById", key = "#entity.id"),
                    @CachePut(value = "fileInfoCacheByPath", key = "#entity.filePath")
            }
    )
    public FileInfoEntity saveFileInfoEntity(FileInfoEntity entity) {
        return fileInfoRepository.save(entity);
    }

    @Cacheable(value = "fileInfoCache", key = "#id")
    public FileInfoEntity getFileEntityById(UUID id) {
        return fileInfoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("File not found"));
    }

    @Cacheable(value = "fileInfoCacheByPath", key = "#path")
    public FileInfoEntity getFileEntityByPath(String path) {
        return fileInfoRepository.findByFilePath(path)
                .orElseThrow(() -> new NotFoundException("File not found"));
    }
}
