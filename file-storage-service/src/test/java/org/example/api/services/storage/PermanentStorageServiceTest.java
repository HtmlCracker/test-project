package org.example.api.services.storage;

import org.example.api.dto.service.StoredFileDto;
import org.example.api.entities.FolderEntity;
import org.example.api.utils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PermanentStorageServiceTest {
    @Mock
    private FolderService folderService;

    @Mock
    private FileUtils fileUtils;

    @InjectMocks
    private PermanentStorageService permanentStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(permanentStorageService, "permanentStoragePath", tempDir.toString());
    }

    @Test
    void permanentUploadFile_shouldSuccessfullyUploadFile() throws Exception {
        String sourceFileName = "test.txt";
        Path sourceFilePath = tempDir.resolve(sourceFileName);
        Files.write(sourceFilePath, "Test content".getBytes());

        FolderEntity mockFolder = FolderEntity.builder()
                .path(tempDir.resolve("storage").toString())
                .build();

        String expectedStoredPath = tempDir.resolve("storage").resolve(sourceFileName).toString();

        when(folderService.getLeastFilledFolder()).thenReturn(mockFolder);
        when(fileUtils.getFileName(anyString())).thenReturn(sourceFileName);
        when(fileUtils.getFileOrThrowException(anyString())).thenReturn(sourceFilePath.toFile());
        when(fileUtils.createFileInDir(anyString(), any(File.class), anyString()))
                .thenReturn(expectedStoredPath);

        StoredFileDto result = permanentStorageService.permanentUploadFile(sourceFilePath.toString());

        assertNotNull(result);
        assertEquals(expectedStoredPath, result.getPath());
        assertEquals(sourceFilePath.toFile().length(), result.getFileSize());
        assertSame(mockFolder, result.getFolderEntity());

        verify(folderService).getLeastFilledFolder();
        verify(fileUtils).getFileName(sourceFilePath.toString());
        verify(fileUtils).getFileOrThrowException(sourceFilePath.toString());
        verify(fileUtils).createFileInDir(sourceFileName, sourceFilePath.toFile(), mockFolder.getPath());
    }

    @Test
    void permanentUploadFile_shouldUseConfiguredStoragePath() throws Exception {
        String customStoragePath = tempDir.resolve("custom-storage").toString();
        ReflectionTestUtils.setField(permanentStorageService, "permanentStoragePath", customStoragePath);

        String sourceFileName = "test.txt";
        Path sourceFilePath = tempDir.resolve(sourceFileName);
        Files.write(sourceFilePath, "Test content".getBytes());

        FolderEntity mockFolder = FolderEntity.builder()
                .path(customStoragePath + "/folder1")
                .build();

        when(folderService.getLeastFilledFolder()).thenReturn(mockFolder);
        when(fileUtils.getFileName(anyString())).thenReturn(sourceFileName);
        when(fileUtils.getFileOrThrowException(anyString())).thenReturn(sourceFilePath.toFile());
        when(fileUtils.createFileInDir(anyString(), any(File.class), anyString()))
                .thenAnswer(invocation -> {
                    String folderPath = invocation.getArgument(2);
                    return folderPath + "/" + invocation.getArgument(0);
                });

        StoredFileDto result = permanentStorageService.permanentUploadFile(sourceFilePath.toString());

        assertNotNull(result);
        assertTrue(result.getPath().startsWith(customStoragePath));
        verify(folderService).getLeastFilledFolder();
    }

    @Test
    void permanentUploadFile_shouldHandleEmptyFile() throws Exception {
        String sourceFileName = "empty.txt";
        Path sourceFilePath = tempDir.resolve(sourceFileName);
        Files.createFile(sourceFilePath);

        FolderEntity mockFolder = FolderEntity.builder()
                .path(tempDir.resolve("storage").toString())
                .build();

        when(folderService.getLeastFilledFolder()).thenReturn(mockFolder);
        when(fileUtils.getFileName(anyString())).thenReturn(sourceFileName);
        when(fileUtils.getFileOrThrowException(anyString())).thenReturn(sourceFilePath.toFile());
        when(fileUtils.createFileInDir(anyString(), any(File.class), anyString()))
                .thenReturn(tempDir.resolve("storage").resolve(sourceFileName).toString());

        StoredFileDto result = permanentStorageService.permanentUploadFile(sourceFilePath.toString());

        assertNotNull(result);
        assertEquals(0, result.getFileSize());
    }
}
