package org.example.api.services;

import org.example.api.dto.response.DeleteFileResponseDto;
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
import org.example.api.statemachine.state.upload.UploadFileState;
import org.example.api.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileOperationServiceTest {
    @Mock
    FileInfoCacheService fileInfoCacheService;

    @Mock
    CompressorService compressorService;

    @Mock
    PermanentStorageService permanentStorageService;

    @Mock
    FolderRepository folderRepository;

    @Mock
    FileInfoRepository fileInfoRepository;

    @Mock
    EncryptorService encryptorService;

    @Mock
    FileUtils fileUtils;

    @InjectMocks
    FileOperationService fileOperationService;

    @Test
    void getFileMimeType_shouldReturnString() {
        String id = "550e8400-e29b-41d4-a716-446655440000";
        UUID uuid = UUID.fromString(id);

        FileInfoEntity mockEntity = FileInfoEntity.builder()
                .id(uuid)
                .mimeType("testMimeType")
                .build();

        when(fileInfoCacheService.getFileEntityById(uuid)).thenReturn(mockEntity);

        String actualMimeType = fileOperationService.getFileMimeType(id);

        assertEquals(mockEntity.getMimeType(), actualMimeType);

        verify(fileInfoCacheService).getFileEntityById(uuid);
    }

    @Test
    void compress_shouldOk() {
        String id = "550e8400-e29b-41d4-a716-446655440000";
        UUID fileId = UUID.fromString(id);
        String originalPath = "test/path/to/original/file";
        String newPath = "test/path/to/compressed/file";
        long compressedSize = 1024L;

        FileInfoEntity mockEntity = FileInfoEntity.builder()
                .id(fileId)
                .originalFileName("testName")
                .originalFileSize(2048L)
                .filePath(originalPath)
                .mimeType("testMimeType")
                .fileExt("testExt")
                .fileState(UploadFileState.UPLOADED)
                .fileHash("testFileHash")
                .currentSize(2048L)
                .build();

        CompressedFileDto mockDto = CompressedFileDto.builder()
                .path(newPath)
                .compressedSize(compressedSize)
                .build();

        when(fileInfoCacheService.getFileEntityById(fileId)).thenReturn(mockEntity);
        when(compressorService.compressFileAndWrite(originalPath)).thenReturn(mockDto);
        doNothing().when(fileUtils).deleteFile(originalPath);
        when(fileInfoCacheService.getFileEntityByPath(originalPath)).thenReturn(mockEntity);

        fileOperationService.compress(id);

        verify(compressorService).compressFileAndWrite(originalPath);
        verify(fileUtils).deleteFile(originalPath);

        ArgumentCaptor<FileInfoEntity> entityCaptor = ArgumentCaptor.forClass(FileInfoEntity.class);
        verify(fileInfoCacheService).saveFileInfoEntity(entityCaptor.capture());

        FileInfoEntity savedEntity = entityCaptor.getValue();
        assertEquals(newPath, savedEntity.getFilePath());
        assertEquals(compressedSize, savedEntity.getCurrentSize());
        assertEquals(UploadFileState.COMPRESSED, savedEntity.getFileState());
    }

    @Test
    void compress_shouldThrowNotFoundExceptionIfFileNotExists() {
        UUID nonExistentId = UUID.randomUUID();
        when(fileInfoCacheService.getFileEntityById(nonExistentId))
                .thenThrow(new NotFoundException("File not found"));

        assertThrows(NotFoundException.class, () -> fileOperationService.compress(nonExistentId.toString()));
    }

    @Test
    void encrypt_shouldOk() {
        String id = "550e8400-e29b-41d4-a716-446655440000";
        UUID fileId = UUID.fromString(id);
        String originalPath = "test/path/to/original/file";
        String newPath = "test/path/to/compressed/file";
        long encryptedSize = 2060L;

        FileInfoEntity mockEntity = FileInfoEntity.builder()
                .id(fileId)
                .originalFileName("testName")
                .originalFileSize(2048L)
                .filePath(originalPath)
                .mimeType("testMimeType")
                .fileExt("testExt")
                .fileState(UploadFileState.COMPRESSED)
                .fileHash("testFileHash")
                .currentSize(2048L)
                .build();

        EncryptedFileDto mockDto = EncryptedFileDto.builder()
                .path(newPath)
                .encryptedSize(encryptedSize)
                .build();

        when(fileInfoCacheService.getFileEntityById(fileId)).thenReturn(mockEntity);
        when(encryptorService.encryptFileAndWrite(originalPath)).thenReturn(mockDto);
        doNothing().when(fileUtils).deleteFile(originalPath);
        when(fileInfoCacheService.getFileEntityByPath(originalPath)).thenReturn(mockEntity);

        fileOperationService.encrypt(id);

        verify(encryptorService).encryptFileAndWrite(originalPath);
        verify(fileUtils).deleteFile(originalPath);

        ArgumentCaptor<FileInfoEntity> entityCaptor = ArgumentCaptor.forClass(FileInfoEntity.class);
        verify(fileInfoCacheService).saveFileInfoEntity(entityCaptor.capture());

        FileInfoEntity savedEntity = entityCaptor.getValue();
        assertEquals(newPath, savedEntity.getFilePath());
        assertEquals(encryptedSize, savedEntity.getCurrentSize());
        assertEquals(UploadFileState.ENCRYPTED, savedEntity.getFileState());
    }

    @Test
    void encrypt_shouldThrowNotFoundExceptionIfFileNotExists() {
        UUID nonExistentId = UUID.randomUUID();
        when(fileInfoCacheService.getFileEntityById(nonExistentId))
                .thenThrow(new NotFoundException("File not found"));

        assertThrows(NotFoundException.class, () -> fileOperationService.encrypt(nonExistentId.toString()));
    }

    @Test
    void store_shouldOk() {
        String id = "550e8400-e29b-41d4-a716-446655440000";
        UUID fileId = UUID.fromString(id);
        String originalPath = "test/path/to/original/file";
        String newPath = "test/path/to/compressed/file";
        long storedSize = 2060L;

        FileInfoEntity mockEntity = FileInfoEntity.builder()
                .id(fileId)
                .originalFileName("testName")
                .originalFileSize(2048L)
                .filePath(originalPath)
                .mimeType("testMimeType")
                .fileExt("testExt")
                .fileState(UploadFileState.COMPRESSED)
                .fileHash("testFileHash")
                .currentSize(2048L)
                .build();

        FolderEntity folderEntity = FolderEntity.builder()
                .id(UUID.randomUUID())
                .folderName("test")
                .fileCount(0)
                .usedStorageByte(2048L)
                .files(new ArrayList<>())
                .build();

        StoredFileDto storedFileDto = StoredFileDto.builder()
                .path(newPath)
                .fileSize(storedSize)
                .folderEntity(folderEntity)
                .build();

        when(fileInfoCacheService.getFileEntityById(fileId)).thenReturn(mockEntity);
        when(permanentStorageService.permanentUploadFile(originalPath)).thenReturn(storedFileDto);
        when(fileInfoCacheService.getFileEntityByPath(originalPath)).thenReturn(mockEntity);
        when(fileInfoCacheService.saveFileInfoEntity(any(FileInfoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(folderRepository.save(any(FolderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fileOperationService.store(id);

        verify(fileInfoCacheService).getFileEntityById(fileId);
        verify(permanentStorageService).permanentUploadFile(originalPath);
        verify(fileUtils).deleteFile(originalPath);
        verify(fileInfoCacheService).getFileEntityByPath(originalPath);

        ArgumentCaptor<FileInfoEntity> fileEntityCaptor = ArgumentCaptor.forClass(FileInfoEntity.class);
        verify(fileInfoCacheService).saveFileInfoEntity(fileEntityCaptor.capture());
        FileInfoEntity savedFileEntity = fileEntityCaptor.getValue();

        assertEquals(newPath, savedFileEntity.getFilePath());
        assertEquals(storedSize, savedFileEntity.getCurrentSize());
        assertEquals(UploadFileState.STORED, savedFileEntity.getFileState());
        assertEquals(folderEntity, savedFileEntity.getFolder());

        ArgumentCaptor<FolderEntity> folderCaptor = ArgumentCaptor.forClass(FolderEntity.class);
        verify(folderRepository).save(folderCaptor.capture());
        FolderEntity savedFolder = folderCaptor.getValue();

        assertEquals(1, savedFolder.getFileCount());
        assertEquals(2048L + storedSize, savedFolder.getUsedStorageByte());
        assertTrue(savedFolder.getFiles().contains(savedFileEntity));
    }

    @Test
    void delete_shouldOk() {
        UUID id = UUID.randomUUID();
        String path = "test/path";

        FileInfoEntity entity = FileInfoEntity.builder()
                .id(id)
                .filePath(path)
                .build();

        DeleteFileResponseDto mockDto = DeleteFileResponseDto.builder()
                .status("deleted")
                .build();

        when(fileInfoRepository.findById(id)).thenReturn(Optional.of(entity));
        doNothing().when(fileUtils).deleteFile(path);
        doNothing().when(fileInfoCacheService).deleteFile(entity);

        assertEquals(mockDto, fileOperationService.delete(id));
    }

    @Test
    void delete_whenFileNotFound_shouldThrowNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();

        when(fileInfoRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            fileOperationService.delete(nonExistentId);
        });

        String expectedMessage = String.format("Entity with id %s if not exists", nonExistentId);
        assertEquals(expectedMessage, exception.getMessage());

        verify(fileInfoRepository).findById(nonExistentId);
        verifyNoInteractions(fileUtils);
        verifyNoInteractions(fileInfoCacheService);
    }

    @Test
    void prepare_shouldMoveFileToPreparedStorage() {
        String originalPath = "test/path/testfile.txt";
        String fileName = "testfile.txt";
        String expectedNewPath = "prepared/storage/path/testfile.txt";

        ReflectionTestUtils.setField(fileOperationService, "preparedForGetStoragePath", "prepared/storage/path");

        when(fileUtils.getFileName(originalPath)).thenReturn(fileName);
        when(fileUtils.moveFileTo(originalPath, "prepared/storage/path/" + fileName))
                .thenReturn(expectedNewPath);

        String resultPath = fileOperationService.prepare(originalPath);

        verify(fileUtils).getFileName(originalPath);
        verify(fileUtils).moveFileTo(originalPath, "prepared/storage/path/" + fileName);

        assertEquals(expectedNewPath, resultPath);
    }

    @Test
    void decrypt_shouldOk() {
        String path = "old/path";
        String newPath = "new/path";

        when(encryptorService.decryptFileAndWrite(path)).thenReturn(newPath);
        doNothing().when(fileUtils).deleteFile(path);

        String resultPath = fileOperationService.decrypt(path);

        verify(encryptorService).decryptFileAndWrite(path);
        verify(fileUtils).deleteFile(path);

        assertEquals(newPath, resultPath);
    }

    @Test
    void decompress_shouldOk() {
        String path = "old/path";
        String newPath = "new/path";

        when(compressorService.decompressFileAndWrite(path)).thenReturn(newPath);
        doNothing().when(fileUtils).deleteFile(path);

        String resultPath = fileOperationService.decompress(path);

        verify(compressorService).decompressFileAndWrite(path);
        verify(fileUtils).deleteFile(path);

        assertEquals(newPath, resultPath);
    }

    @Test
    void deliver_shouldMoveFileToReadyStorageAndDeleteOriginal() {
        String originalPath = "old/path.test";
        String fileName = "path.test";
        String readyStoragePath = "ready/storage";
        String expectedNewPath = readyStoragePath + "/" + fileName;

        ReflectionTestUtils.setField(fileOperationService, "pathToReadyForGetStorage", readyStoragePath);

        when(fileUtils.getFileName(originalPath)).thenReturn(fileName);
        when(fileUtils.moveFileTo(originalPath, expectedNewPath)).thenReturn(expectedNewPath);
        doNothing().when(fileUtils).deleteFile(originalPath);

        String resultPath = fileOperationService.deliver(originalPath);

        verify(fileUtils).getFileName(originalPath);
        verify(fileUtils).moveFileTo(originalPath, expectedNewPath);
        verify(fileUtils).deleteFile(originalPath);

        assertEquals(expectedNewPath, resultPath);
    }
}