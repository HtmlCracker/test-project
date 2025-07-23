package org.example.api.services.storage;

import org.example.api.entities.FileInfoEntity;
import org.example.api.services.FileInfoCacheService;
import org.example.api.statemachine.state.upload.UploadFileState;
import org.example.api.utils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TemporaryStorageServiceTest {
    @Mock
    private FileUtils fileUtils;

    @Mock
    private FileInfoCacheService fileInfoCacheService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private TemporaryStorageService temporaryStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(temporaryStorageService, "temporaryStoragePath", tempDir.toString());
    }

    @Test
    void temporaryUploadFile_shouldSuccessfullyProcessFile() throws Exception {
        // Arrange
        String originalFilename = "test.txt";
        String fileHash = "abc123";
        String fileExt = "txt";
        String mimeType = "text/plain";
        long fileSize = 1024L;
        String expectedFileName = fileHash + "." + fileExt;
        String expectedFilePath = tempDir.resolve(expectedFileName).toString();

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(fileUtils.calculateUniqueFileHash(multipartFile)).thenReturn(fileHash);
        when(fileUtils.getFileExtension(originalFilename)).thenReturn(fileExt);
        when(fileUtils.getFileMime(multipartFile)).thenReturn(mimeType);
        when(fileUtils.createFileInDir(expectedFileName, multipartFile, tempDir.toString()))
                .thenReturn(expectedFilePath);

        FileInfoEntity expectedEntity = FileInfoEntity.builder()
                .originalFileName(originalFilename)
                .originalFileSize(fileSize)
                .mimeType(mimeType)
                .fileExt(fileExt)
                .fileState(UploadFileState.UPLOADED)
                .fileHash(fileHash)
                .filePath(expectedFilePath)
                .build();

        when(fileInfoCacheService.saveFileInfoEntity(any(FileInfoEntity.class))).thenReturn(expectedEntity);

        // Act
        FileInfoEntity result = temporaryStorageService.temporaryUploadFile(multipartFile);

        // Assert
        assertNotNull(result);
        assertEquals(originalFilename, result.getOriginalFileName());
        assertEquals(fileSize, result.getOriginalFileSize());
        assertEquals(mimeType, result.getMimeType());
        assertEquals(fileExt, result.getFileExt());
        assertEquals(UploadFileState.UPLOADED, result.getFileState());
        assertEquals(fileHash, result.getFileHash());
        assertEquals(expectedFilePath, result.getFilePath());

        verify(fileUtils).calculateUniqueFileHash(multipartFile);
        verify(fileUtils, times(2)).getFileExtension(originalFilename); // Исправлено: 2 вызова
        verify(fileUtils).getFileMime(multipartFile);
        verify(fileUtils).createFileInDir(expectedFileName, multipartFile, tempDir.toString());
        verify(fileInfoCacheService).saveFileInfoEntity(any(FileInfoEntity.class));
    }


}
