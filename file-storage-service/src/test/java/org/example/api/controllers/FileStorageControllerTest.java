package org.example.api.controllers;

import org.example.api.dto.response.DeleteFileResponseDto;
import org.example.api.dto.response.UploadFileResponseDto;
import org.example.api.entities.FileInfoEntity;
import org.example.api.exceptions.BadRequestException;
import org.example.api.factories.response.UploadFileResponseDtoFactory;
import org.example.api.services.FileInfoCacheService;
import org.example.api.services.FileOperationService;
import org.example.api.services.FileStateMachineService;
import org.example.api.services.storage.TemporaryStorageService;
import org.example.api.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileStorageControllerTest {
    @Mock
    private FileInfoCacheService fileInfoCacheService;

    @Mock
    private UploadFileResponseDtoFactory uploadFileResponseDtoFactory;

    @Mock
    private TemporaryStorageService temporaryStorageService;

    @Mock
    private FileStateMachineService fileProcessorService;

    @Mock
    private FileOperationService fileStorageService;

    @Mock
    private FileUtils fileUtils;

    @InjectMocks
    private FileStorageController fileStorageController;

    @TempDir
    Path tempDir;

    @Test
    void uploadFile_shouldReturnResponseDtoWhenFileValid() {
        MultipartFile mockFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());

        FileInfoEntity mockEntity = new FileInfoEntity();
        mockEntity.setId(UUID.randomUUID());

        when(temporaryStorageService.temporaryUploadFile(mockFile)).thenReturn(mockEntity);
        when(fileInfoCacheService.getFileEntityById(mockEntity.getId())).thenReturn(mockEntity);

        UploadFileResponseDto expectedDto = new UploadFileResponseDto();
        when(uploadFileResponseDtoFactory.makeUploadFileResponseDto(mockEntity)).thenReturn(expectedDto);

        UploadFileResponseDto result = fileStorageController.uploadFile(mockFile);

        assertSame(expectedDto, result);
        verify(fileProcessorService).uploadFile(mockEntity.getId().toString());
    }

    @Test
    void uploadFile_shouldThrowBadRequestExceptionWhenFileEmpty() {
        MultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(BadRequestException.class, () -> {
            fileStorageController.uploadFile(emptyFile);
        });
    }

    @Test
    void deleteFile_shouldCallServiceAndReturnResponse() {
        UUID fileId = UUID.randomUUID();
        DeleteFileResponseDto expectedResponse = new DeleteFileResponseDto();
        when(fileStorageService.delete(fileId)).thenReturn(expectedResponse);

        DeleteFileResponseDto result = fileStorageController.deleteFile(fileId);

        assertSame(expectedResponse, result);
    }

    @Test
    void getFile_shouldReturnStreamingResponseWithCorrectHeaders(@TempDir Path tempDir) throws IOException {
        UUID fileId = UUID.randomUUID();
        String originalFileName = "test-file.txt";
        String fileContent = "Test file content";
        String filePath = tempDir.resolve("test-file.txt").toString();
        String encryptionKey = "test";

        File testFile = tempDir.resolve("test-file.txt").toFile();
        Files.write(testFile.toPath(), fileContent.getBytes());

        FileInfoEntity mockEntity = new FileInfoEntity();
        mockEntity.setId(fileId);
        mockEntity.setOriginalFileName(originalFileName);
        mockEntity.setFilePath(filePath);

        when(fileInfoCacheService.getFileEntityById(fileId)).thenReturn(mockEntity);
        when(fileProcessorService.getFile(filePath, null)).thenReturn(filePath);
        when(fileUtils.getFileOrThrowException(filePath)).thenReturn(testFile);

        ResponseEntity<StreamingResponseBody> response = fileStorageController.getFile(fileId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        response.getBody().writeTo(outputStream);

        assertNotNull(response);
        assertEquals(fileContent.length(), response.getHeaders().getContentLength());

        HttpHeaders headers = response.getHeaders();
        String encodedFilename = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        assertEquals("attachment; filename=\"" + encodedFilename + "\"",
                headers.getFirst("Content-Disposition"));
        assertEquals(encodedFilename, headers.getFirst("X-File-Name"));
        assertEquals(String.valueOf(fileContent.length()), headers.getFirst("X-File-Size"));

        verify(fileUtils).deleteFile(filePath);
    }

    @Test
    void getFile_shouldThrowExceptionWhenFileNotFound() {
        UUID fileId = UUID.randomUUID();
        String filePath = "/nonexistent/path";
        String encryptionKey = "test";

        FileInfoEntity mockEntity = new FileInfoEntity();
        mockEntity.setId(fileId);
        mockEntity.setFilePath(filePath);

        when(fileInfoCacheService.getFileEntityById(fileId)).thenReturn(mockEntity);
        when(fileProcessorService.getFile(filePath, null)).thenReturn(filePath);
        when(fileUtils.getFileOrThrowException(filePath)).thenThrow(new BadRequestException("File not found"));

        assertThrows(BadRequestException.class, () -> {
            fileStorageController.getFile(fileId);
        });
    }
}
