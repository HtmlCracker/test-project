package org.example.api.utils;

import org.example.api.exceptions.BadRequestException;
import org.example.api.exceptions.FileNotFoundException;
import org.example.api.exceptions.FileProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileUtilsTest {
    @InjectMocks
    FileUtils fileUtils;

    @Mock
    MultipartFile mockMultipartFile;

    @TempDir
    Path tempDir;

    @Mock
    private File mockFile;

    @Test
    void createFileInDir_withMultipartFile_shouldOk() {
        String fileName = "test.test";
        String pathToDir = "testDir";
        String fileContent = "Test file content";

        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                fileName,
                "text/plain",
                fileContent.getBytes(StandardCharsets.UTF_8)
        );

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            String result = fileUtils.createFileInDir(fileName, multipartFile, pathToDir);

            filesMock.verify(() ->
                    Files.copy(
                            any(InputStream.class),
                            eq(Paths.get(pathToDir, fileName)),
                            eq(StandardCopyOption.REPLACE_EXISTING)
                    )
            );

            assertThat(result).isEqualTo(Paths.get(pathToDir, fileName).toString());
        }
    }

    @Test
    void createFileInDir_whenGetInputStreamThrowsIOException_shouldThrowBadRequestException() throws IOException {
        when(mockMultipartFile.getInputStream()).thenThrow(new IOException("Failed to read file"));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> fileUtils.createFileInDir("test.txt", mockMultipartFile, "testDir")
        );

        assertEquals("File can't be empty or inaccessible", exception.getMessage());
        verify(mockMultipartFile, times(1)).getInputStream();
    }

    @Test
    void createFileInDir_withRealFile_shouldWork() throws IOException {
        File testFile = tempDir.resolve("real_test.txt").toFile();
        Files.write(testFile.toPath(), "Test content".getBytes());

        FileUtils fileUtils = new FileUtils();
        String result = fileUtils.createFileInDir(
                "output.txt",
                testFile,
                tempDir.toString()
        );

        Path expectedPath = tempDir.resolve("output.txt");
        assertTrue(Files.exists(expectedPath), "Файл не создался");
        assertEquals(expectedPath.toString(), result);
    }

    @Test
    void createFileInDir_whenFileDoesNotExist_shouldThrowException() {
        File nonExistentFile = tempDir.resolve("ghost.txt").toFile();

        FileUtils fileUtils = new FileUtils();
        assertThrows(
                BadRequestException.class,
                () -> fileUtils.createFileInDir("output.txt", nonExistentFile, tempDir.toString())
        );
    }

    @Test
    void createFileInDir_shouldSuccessfullyCreateFile() throws IOException {
        String fileName = "test.txt";
        String pathToDir = "testDir";
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any()))
                    .thenReturn(1L);

            String result = fileUtils.createFileInDir(fileName, inputStream, pathToDir);

            assertEquals(Paths.get(pathToDir, fileName).toString(), result);
            mockedFiles.verify(() -> Files.createDirectories(any(Path.class)));
            mockedFiles.verify(() -> Files.copy(any(InputStream.class), any(Path.class), any()));
        }
    }

    @Test
    void createFileInDir_shouldThrowWhenInputStreamIsNull() {
        String fileName = "test.txt";
        String pathToDir = "testDir";

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> fileUtils.createFileInDir(fileName, (InputStream) null, pathToDir));

        assertEquals("Input stream is null", exception.getMessage());
    }

    @Test
    void createFileInDir_shouldThrowWhenCreateDirectoryFails() throws IOException {
        String fileName = "test.txt";
        String pathToDir = "testDir";
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("Disk full"));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> fileUtils.createFileInDir(fileName, inputStream, pathToDir));

            assertEquals("Failed to create directory: " + Paths.get(pathToDir).toAbsolutePath().normalize(),
                    exception.getMessage());
        }
    }

    @Test
    void createFileInDir_shouldThrowWhenCopyFails() throws IOException {
        String fileName = "test.txt";
        String pathToDir = "testDir";
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenReturn(Paths.get(pathToDir));
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), any()))
                    .thenThrow(new IOException("Permission denied"));

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> fileUtils.createFileInDir(fileName, inputStream, pathToDir));

            assertEquals("Save file error: Permission denied", exception.getMessage());
        }
    }

    @Test
    void moveFileTo_shouldSuccessfullyMoveFile() throws IOException {
        String sourcePath = "source.txt";
        String destPath = "dest.txt";
        Path absDestPath = Paths.get(destPath).toAbsolutePath();

        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(Path.class), any(Path.class), any()))
                    .thenReturn(absDestPath);

            String result = fileUtils.moveFileTo(sourcePath, destPath);

            assertEquals(absDestPath.toString(), result);
            mockedFiles.verify(() ->
                    Files.copy(
                            Paths.get(sourcePath),
                            Paths.get(destPath),
                            StandardCopyOption.REPLACE_EXISTING
                    )
            );
        }
    }

    @Test
    void moveFileTo_shouldThrowWhenFileAlreadyExists() throws IOException {
        String sourcePath = "source.txt";
        String destPath = "dest.txt";

        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(Path.class), any(Path.class), any()))
                    .thenThrow(new FileAlreadyExistsException(destPath));

            FileProcessingException exception = assertThrows(FileProcessingException.class,
                    () -> fileUtils.moveFileTo(sourcePath, destPath));

            assertEquals("File already exists", exception.getMessage());
        }
    }

    @Test
    void moveFileTo_shouldThrowWhenIOErrorOccurs() throws IOException {
        String sourcePath = "source.txt";
        String destPath = "dest.txt";

        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(Path.class), any(Path.class), any()))
                    .thenThrow(new IOException("Disk error"));

            FileProcessingException exception = assertThrows(FileProcessingException.class,
                    () -> fileUtils.moveFileTo(sourcePath, destPath));

            assertEquals("IOException", exception.getMessage());
        }
    }

    @Test
    void moveFileTo_shouldReturnAbsolutePath() throws IOException {
        String sourcePath = "source.txt";
        String destPath = "dest.txt";
        Path absPath = Paths.get(destPath).toAbsolutePath();

        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(any(Path.class), any(Path.class), any()))
                    .thenReturn(absPath);

            String result = fileUtils.moveFileTo(sourcePath, destPath);

            assertEquals(absPath.toString(), result);
            assertTrue(result.contains(destPath));
        }
    }

    @Test
    void deleteFile_shouldSuccessfullyDeleteFile(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.createFile(testFile);

        fileUtils.deleteFile(testFile.toString());

        assertFalse(Files.exists(testFile));
    }

    @Test
    void deleteFile_shouldThrowWhenFileNotExists() {
        String nonExistentPath = "nonexistent.txt";

        assertThrows(FileNotFoundException.class,
                () -> fileUtils.deleteFile(nonExistentPath));
    }

    @Test
    void deleteFile_shouldThrowWhenDeleteFails(@TempDir Path tempDir) throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.createFile(testFile);

        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.delete(any(Path.class)))
                    .thenThrow(new IOException("Permission denied"));

            FileProcessingException exception = assertThrows(FileProcessingException.class,
                    () -> fileUtils.deleteFile(testFile.toString()));

            assertEquals(
                    String.format("Error processing file with path: %s", testFile.toString()),
                    exception.getMessage()
            );
        }
    }

    @Test
    void calculateFileHash_shouldCalculateCorrectSHA256Hash() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenReturn("test content".getBytes());

        String hash = fileUtils.calculateFileHash(mockFile);

        assertNotNull(hash);
        assertEquals(64, hash.length());
        verify(mockFile).getBytes();
    }

    @Test
    void calculateFileHash_shouldThrowWhenIOError() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenThrow(new IOException("File error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> fileUtils.calculateFileHash(mockFile));

        assertEquals("Failed to calculate file hash", exception.getMessage());
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void calculateFileHash_shouldThrowWhenNoSuchAlgorithm() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenReturn("test".getBytes());

        try (var mockedDigest = mockStatic(MessageDigest.class)) {
            mockedDigest.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("Algorithm not found"));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> fileUtils.calculateFileHash(mockFile));

            assertEquals("Failed to calculate file hash", exception.getMessage());
            assertTrue(exception.getCause() instanceof NoSuchAlgorithmException);
        }
    }

    @Test
    void calculateUniqueFileHash_shouldCombineHashWithTimestamp() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenReturn("test".getBytes());

        String result = fileUtils.calculateUniqueFileHash(mockFile);

        assertNotNull(result);
        assertTrue(result.substring(64).matches("\\d+"));
        assertEquals(64 + 13, result.length());
    }

    @Test
    void calculateUniqueFileHash_shouldUseCalculateFileHash() {
        FileUtils fileUtils = spy(new FileUtils());
        MultipartFile mockFile = mock(MultipartFile.class);

        doReturn("testhash").when(fileUtils).calculateFileHash(mockFile);

        String result = fileUtils.calculateUniqueFileHash(mockFile);

        assertTrue(result.startsWith("testhash"));
        verify(fileUtils).calculateFileHash(mockFile);
    }

    @Test
    void getFileExtension_shouldReturnExtension() {
        String fileName = "test.txt";

        String answer = fileUtils.getFileExtension(fileName);

        assertEquals(answer, "txt");
    }

    @Test
    void getFileExtension_shouldThrowBadRequestExceptionWhenFileNameIsEmpty() {
        String emptyFileName = "";

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> fileUtils.getFileExtension(emptyFileName));

        assertEquals("File name can't be empty", exception.getMessage());
    }

    @Test
    void getFileMimeFromFile_shouldReturnMimeType() throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "test content".getBytes());

        String result = fileUtils.getFileMime(testFile.toFile());

        assertTrue(result.equals("text") || result.equals("unknown mime type"));
    }

    @Test
    void getFileMimeFromFile_shouldReturnUnknownWhenNull() throws IOException {
        Path testFile = tempDir.resolve("test.unknown");
        Files.write(testFile, "test content".getBytes());

        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.probeContentType(any(Path.class)))
                    .thenReturn(null);

            String result = fileUtils.getFileMime(testFile.toFile());

            assertEquals("unknown mime type", result);
        }
    }

    @Test
    void getFileMimeFromFile_shouldReturnFirstPartOfMimeType() throws IOException {
        File mockFile = mock(File.class);
        when(mockFile.toPath()).thenReturn(Path.of("test"));

        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.probeContentType(any(Path.class)))
                    .thenReturn("image/png");

            String result = fileUtils.getFileMime(mockFile);

            assertEquals("image", result);
        }
    }

    @Test
    void getFileMimeFromMultipart_shouldReturnMimeType() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn("application/pdf");

        String result = fileUtils.getFileMime(mockFile);

        assertEquals("application", result);
    }

    @Test
    void getFileMimeFromMultipart_shouldReturnUnknownWhenNull() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn(null);

        String result = fileUtils.getFileMime(mockFile);

        assertEquals("unknown mime type", result);
    }

    @Test
    void getFileMimeFromMultipart_shouldReturnFirstPart() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getContentType()).thenReturn("text/plain");

        String result = fileUtils.getFileMime(mockFile);

        assertEquals("text", result);
    }

    @Test
    void getFileName_shouldReturnFileNameFromPath() {
        String path = "/home/user/documents/report.pdf";

        String result = fileUtils.getFileName(path);

        assertEquals("report.pdf", result);
    }

    @Test
    void getFileName_shouldReturnFileNameForRelativePath() {
        String path = "../data/files/image.jpg";

        String result = fileUtils.getFileName(path);

        assertEquals("image.jpg", result);
    }

    @Test
    void getFileName_shouldReturnSingleFile() {
        String path = "file.txt";

        String result = fileUtils.getFileName(path);

        assertEquals("file.txt", result);
    }

    @Test
    void getFileMime_shouldThrowRuntimeExceptionWhenProbeContentTypeFails() throws IOException {
        File mockFile = mock(File.class);
        Path mockPath = mock(Path.class);

        when(mockFile.toPath()).thenReturn(mockPath);

        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.probeContentType(mockPath))
                    .thenThrow(new IOException("Failed to probe content type"));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> fileUtils.getFileMime(mockFile));

            assertEquals("Failed to probe content type", exception.getCause().getMessage());
            assertTrue(exception.getCause() instanceof IOException);
        }
    }
}