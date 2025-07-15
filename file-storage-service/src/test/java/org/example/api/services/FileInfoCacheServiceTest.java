package org.example.api.services;

import org.example.api.entities.FileInfoEntity;
import org.example.api.exceptions.NotFoundException;
import org.example.api.repositories.FileInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileInfoCacheServiceTest {
    @Mock
    private FileInfoRepository fileInfoRepository;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private FileInfoCacheService fileInfoCacheService;

    @Test
    void saveFileInfoEntity_shouldSaveAndCacheEntity() {
        FileInfoEntity entity = new FileInfoEntity();
        entity.setId(UUID.randomUUID());
        entity.setFilePath("/test/path");

        when(fileInfoRepository.saveAndFlush(entity)).thenReturn(entity);

        FileInfoEntity result = fileInfoCacheService.saveFileInfoEntity(entity);

        assertEquals(entity, result);
        verify(fileInfoRepository).saveAndFlush(entity);
    }

    @Test
    void getFileEntityById_shouldReturnCachedEntity() {
        UUID id = UUID.randomUUID();
        FileInfoEntity expectedEntity = new FileInfoEntity();
        expectedEntity.setId(id);

        when(fileInfoRepository.findById(id)).thenReturn(Optional.of(expectedEntity));

        FileInfoEntity result = fileInfoCacheService.getFileEntityById(id);

        assertEquals(expectedEntity, result);
        verify(fileInfoRepository).findById(id);
    }

    @Test
    void getFileEntityById_shouldThrowNotFoundException() {
        UUID id = UUID.randomUUID();
        when(fileInfoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            fileInfoCacheService.getFileEntityById(id);
        });
    }

    @Test
    void getFileEntityByPath_shouldReturnCachedEntity() {
        String path = "/test/path";
        FileInfoEntity expectedEntity = new FileInfoEntity();
        expectedEntity.setFilePath(path);

        when(fileInfoRepository.findByFilePath(path)).thenReturn(Optional.of(expectedEntity));

        FileInfoEntity result = fileInfoCacheService.getFileEntityByPath(path);

        assertEquals(expectedEntity, result);
        verify(fileInfoRepository).findByFilePath(path);
    }

    @Test
    void getFileEntityByPath_shouldThrowNotFoundException() {
        String path = "/nonexistent/path";
        when(fileInfoRepository.findByFilePath(path)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            fileInfoCacheService.getFileEntityByPath(path);
        });
    }

    @Test
    void deleteFile_shouldDeleteAndEvictCache() {
        FileInfoEntity entity = new FileInfoEntity();
        entity.setId(UUID.randomUUID());
        entity.setFilePath("/test/path");

        doNothing().when(fileInfoRepository).delete(entity);

        fileInfoCacheService.deleteFile(entity);

        verify(fileInfoRepository).delete(entity);
    }
}
