package org.example.api.services.storage;

import org.example.api.entities.FolderEntity;
import org.example.api.exceptions.BadRequestException;
import org.example.api.repositories.FolderRepository;
import org.example.api.utils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FolderServiceTest {
    @Mock
    private FolderRepository folderRepository;

    @Mock
    private FileUtils fileUtils;

    @InjectMocks
    private FolderService folderService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(folderService, "maxFileCount", 500);
    }

    @Test
    void createRootIfNotExists_shouldCreateRootWhenNotExists() {
        String storagePath = tempDir.toString();
        when(folderRepository.findByFolderName("root")).thenReturn(null);
        when(folderRepository.save(any(FolderEntity.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        FolderEntity result = folderService.createRootIfNotExists(storagePath);

        assertNotNull(result);
        assertEquals("root", result.getFolderName());
        assertEquals(storagePath + "/root", result.getPath());
        verify(fileUtils).createDirectoryIfNotExists(storagePath + "/root");
        verify(folderRepository).save(any(FolderEntity.class));
    }

    @Test
    void createRootIfNotExists_shouldReturnExistingRoot() {
        String storagePath = tempDir.toString();
        FolderEntity existingRoot = FolderEntity.builder()
                .folderName("root")
                .path(storagePath + "/root")
                .build();
        when(folderRepository.findByFolderName("root")).thenReturn(existingRoot);

        FolderEntity result = folderService.createRootIfNotExists(storagePath);

        assertSame(existingRoot, result);
        verify(fileUtils, never()).createDirectoryIfNotExists(anyString());
        verify(folderRepository, never()).save(any());
    }

    @Test
    void getLeastFilledFolder_shouldReturnFolderWhenUnderMaxCount() {
        FolderEntity folder = FolderEntity.builder()
                .fileCount(100)
                .build();
        when(folderRepository.findWithMinFileCount())
                .thenReturn(Optional.of(new ArrayList<>(List.of(folder))));

        FolderEntity result = folderService.getLeastFilledFolder();

        assertSame(folder, result);
    }

    @Test
    void getLeastFilledFolder_shouldThrowWhenNoFolders() {
        when(folderRepository.findWithMinFileCount())
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> {
            folderService.getLeastFilledFolder();
        });
    }

    @Test
    void getLeastFilledFolder_shouldCreateNewFoldersWhenMaxCountReached() {
        ReflectionTestUtils.setField(folderService, "maxFileCount", 10);

        FolderEntity fullFolder = FolderEntity.builder()
                .fileCount(10)
                .isLast(true)
                .path(tempDir.toString())
                .childrens(new ArrayList<>())
                .build();

        when(folderRepository.findWithMinFileCount())
                .thenReturn(Optional.of(new ArrayList<>(List.of(fullFolder))));

        when(folderRepository.findByIsLast(true))
                .thenReturn(Optional.of(new ArrayList<>(List.of(fullFolder))));

        when(folderRepository.save(any(FolderEntity.class))).thenAnswer(invocation -> {
            FolderEntity saved = invocation.getArgument(0);
            if (saved.getChildrens() == null) {
                saved.setChildrens(new ArrayList<>());
            }
            return saved;
        });

        FolderEntity result = folderService.getLeastFilledFolder();

        assertNotNull(result);
        verify(fileUtils, atLeastOnce()).createDirectoryIfNotExists(anyString());
        verify(folderRepository, atLeast(3)).save(any(FolderEntity.class));
    }}
