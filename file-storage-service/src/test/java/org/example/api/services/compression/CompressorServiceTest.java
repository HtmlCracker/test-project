package org.example.api.services.compression;

import org.example.api.configs.FileTypeConfig;
import org.example.api.dto.service.CompressedFileDto;
import org.example.api.services.compression.interfaces.ComprssionStrategy;
import org.example.api.utils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompressorServiceTest {
    @Mock
    private FileTypeConfig fileTypeConfig;

    @Mock
    private FileUtils fileUtils;

    @InjectMocks
    private CompressorService compressorService;

    @TempDir
    Path tempDir;

    private final String testFilePath = "test.txt";
    private final String textMimeType = "text/plain";
    private final String binaryMimeType = "application/octet-stream";
    private final String compressedExtension = ".compressed";

    @BeforeEach
    void setUp() throws IOException {
        Path compressedDir = tempDir.resolve("compressed");
        Path decompressedDir = tempDir.resolve("decompressed");
        Files.createDirectories(compressedDir);
        Files.createDirectories(decompressedDir);

        ReflectionTestUtils.setField(compressorService, "compressedStoragePath", compressedDir.toString());
        ReflectionTestUtils.setField(compressorService, "decompressedStoragePath", decompressedDir.toString());

        when(fileTypeConfig.getTextType()).thenReturn(textMimeType);
        when(fileTypeConfig.getBinaryType()).thenReturn(binaryMimeType);
        compressorService.initCompressionStrategies();
    }

    @Test
    void compressFileAndWrite_ShouldSuccessfullyCompressTextFile() throws IOException {
        Path sourceFile = tempDir.resolve(testFilePath);
        Files.write(sourceFile, "Test content".getBytes());

        when(fileUtils.getFileOrThrowException(sourceFile.toString())).thenReturn(sourceFile.toFile());
        when(fileUtils.getFileMime(sourceFile.toFile())).thenReturn(textMimeType);

        ComprssionStrategy mockStrategy = mock(ComprssionStrategy.class);
        when(mockStrategy.getCompressedFileExtension()).thenReturn(compressedExtension);

        Map<String, ComprssionStrategy> compressionStrategies = new HashMap<>();
        compressionStrategies.put(textMimeType, mockStrategy);
        ReflectionTestUtils.setField(compressorService, "compressionStrategies", compressionStrategies);

        CompressedFileDto result = compressorService.compressFileAndWrite(sourceFile.toString());

        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result.getPath())));
        verify(mockStrategy).compress(any(), any());
    }

    @Test
    void decompressFileAndWrite_ShouldSuccessfullyDecompressFile() throws IOException {
        String compressedFileName = "test.txt" + compressedExtension;
        Path compressedFile = tempDir.resolve(compressedFileName);
        Files.createFile(compressedFile);

        ComprssionStrategy mockStrategy = mock(ComprssionStrategy.class);
        Map<String, ComprssionStrategy> decompressionStrategies = new HashMap<>();
        decompressionStrategies.put(compressedExtension, mockStrategy);
        ReflectionTestUtils.setField(compressorService, "decompressionStrategies", decompressionStrategies);

        when(fileUtils.getFileOrThrowException(compressedFile.toString())).thenReturn(compressedFile.toFile());
        when(fileUtils.getFileExtension(compressedFile.toString())).thenReturn("compressed"); // без точки!
        when(fileUtils.getFileName(compressedFile.toString())).thenReturn(compressedFileName);

        Path decompressedDir = tempDir.resolve("output");
        Files.createDirectories(decompressedDir);
        ReflectionTestUtils.setField(compressorService, "decompressedStoragePath", decompressedDir.toString());

        String resultPath = compressorService.decompressFileAndWrite(compressedFile.toString());

        assertNotNull(resultPath, "Result path should not be null");

        Path expectedPath = decompressedDir.resolve("test.txt");
        assertEquals(expectedPath.toString(), resultPath, "The output path is incorrect");

        verify(mockStrategy).decompress(any(), any());
    }
}
