package org.example.api.services.compression.impl;

import org.example.api.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TextCompressionStrategyTest {
    @InjectMocks
    TextComprStrategy textCompressionStrategy;

    @Mock
    private InputStream mockInputStream;

    @Mock
    private OutputStream mockOutputStream;

    @TempDir
    Path tempDir;

    @Test
    void getCompressedFileExtension_shouldReturnGzExtension() {
        assertEquals(".gz", textCompressionStrategy.getCompressedFileExtension());
    }

    @Test
    void compress_shouldCompressDataCorrectly(@TempDir Path tempDir) throws IOException {
        String testData = "This is a test string for GZIP compression";
        Path inputFile = tempDir.resolve("input.txt");
        Files.write(inputFile, testData.getBytes());

        Path outputFile = tempDir.resolve("compressed.gz");

        try (InputStream in = Files.newInputStream(inputFile);
             OutputStream out = Files.newOutputStream(outputFile)) {

            textCompressionStrategy.compress(in, out);
        }

        assertTrue(Files.exists(outputFile));
        assertTrue(Files.size(outputFile) > 0);

        try (InputStream in = Files.newInputStream(outputFile);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            textCompressionStrategy.decompress(in, out);

            assertEquals(testData, out.toString());
        }
    }

    @Test
    void decompress_shouldDecompressDataCorrectly(@TempDir Path tempDir) throws IOException {
        String originalText = "Test data to be compressed with GZIP and then decompressed";
        ByteArrayOutputStream compressedOutput = new ByteArrayOutputStream();
        try (InputStream in = new ByteArrayInputStream(originalText.getBytes())) {
            textCompressionStrategy.compress(in, compressedOutput);
        }

        Path compressedFile = tempDir.resolve("test.gz");
        Files.write(compressedFile, compressedOutput.toByteArray());

        Path decompressedFile = tempDir.resolve("decompressed.txt");

        try (InputStream in = Files.newInputStream(compressedFile);
             OutputStream out = Files.newOutputStream(decompressedFile)) {

            textCompressionStrategy.decompress(in, out);
        }

        String decompressedText = Files.readString(decompressedFile);
        assertEquals(originalText, decompressedText);
    }

    @Test
    void compress_shouldThrowBadRequestExceptionOnIOException() throws IOException {
        when(mockInputStream.read(any(byte[].class))).thenThrow(new IOException("Simulated IO error"));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            textCompressionStrategy.compress(mockInputStream, mockOutputStream);
        });

        assertEquals("Gzip compression failed", exception.getMessage());
    }

}
