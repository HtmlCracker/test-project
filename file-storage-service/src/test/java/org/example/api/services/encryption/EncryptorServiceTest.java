package org.example.api.services.encryption;

import org.example.api.dto.service.EncryptedFileDto;
import org.example.api.utils.EncryptionUtils;
import org.example.api.utils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EncryptorServiceTest {
    @Mock
    private FileUtils fileUtils;

    @Mock
    private EncryptionUtils encryptionUtils;

    @InjectMocks
    private EncryptorService encryptorService;

    private final String testEncryptionKey = "test-key";
    private final String testEncryptedPath = "/encrypted/storage";
    private final String testDecryptedPath = "/decrypted/storage";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(encryptorService, "encryptionKey", testEncryptionKey);
        ReflectionTestUtils.setField(encryptorService, "encryptedStoragePath", testEncryptedPath);
        ReflectionTestUtils.setField(encryptorService, "decryptedStoragePath", testDecryptedPath);
    }

    @Test
    void encryptFileAndWrite_ShouldReturnEncryptedFileDto_WhenFileExists() {
        String inputPath = "/path/to/original/file.txt";
        String fileName = "file.txt";
        File originalFile = mock(File.class);
        File encryptedFile = new File(testEncryptedPath, fileName);
        long expectedSize = 1024L;

        when(fileUtils.getFileOrThrowException(inputPath)).thenReturn(originalFile);
        when(fileUtils.getFileName(inputPath)).thenReturn(fileName);
        when(encryptionUtils.encrypt(testEncryptionKey, originalFile, encryptedFile))
                .thenReturn(expectedSize);

        EncryptedFileDto result = encryptorService.encryptFileAndWrite(inputPath);

        assertNotNull(result);
        assertEquals(encryptedFile.getPath(), result.getPath());
        assertEquals(expectedSize, result.getEncryptedSize());

        verify(fileUtils).getFileOrThrowException(inputPath);
        verify(fileUtils).getFileName(inputPath);
        verify(encryptionUtils).encrypt(testEncryptionKey, originalFile, encryptedFile);
    }

    @Test
    void decryptFileAndWrite_ShouldReturnDecryptedFilePath_WhenFileExists() {
        String inputPath = "/path/to/encrypted/file.txt";
        String fileName = "file.txt";
        File encryptedFile = mock(File.class);
        File decryptedFile = new File(testDecryptedPath, fileName);
        long expectedSize = 1024L;

        when(fileUtils.getFileOrThrowException(inputPath)).thenReturn(encryptedFile);
        when(fileUtils.getFileName(inputPath)).thenReturn(fileName);
        when(encryptionUtils.decrypt(testEncryptionKey, encryptedFile, decryptedFile))
                .thenReturn(expectedSize);

        String result = encryptorService.decryptFileAndWrite(inputPath);

        assertEquals(decryptedFile.getPath(), result);

        verify(fileUtils).getFileOrThrowException(inputPath);
        verify(fileUtils).getFileName(inputPath);
        verify(encryptionUtils).decrypt(testEncryptionKey, encryptedFile, decryptedFile);
    }
}
