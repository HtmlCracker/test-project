package org.example.api.services.compression.impl;

import net.jpountz.lz4.LZ4FrameInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BinaryComprStrategyTest {
    @InjectMocks
    BinaryComprStrategy binaryComprStrategy;

    @Mock
    private InputStream mockInputStream;

    @Mock
    private OutputStream mockOutputStream;

    @TempDir
    Path tempDir;

    @Test
    void getCompressedFileExtension_shouldReturnLz4Extension() {
        assertEquals(".lz4", binaryComprStrategy.getCompressedFileExtension());
    }

    @Test
    void compress_shouldCompressDataCorrectly(@TempDir Path tempDir) throws IOException {
        String testData = "This is a test string for compression";
        Path inputFile = tempDir.resolve("input.txt");
        Files.write(inputFile, testData.getBytes());

        Path outputFile = tempDir.resolve("compressed.lz4");

        try (InputStream in = Files.newInputStream(inputFile);
             OutputStream out = Files.newOutputStream(outputFile)) {

            binaryComprStrategy.compress(in, out);
        }

        assertTrue(Files.exists(outputFile));
        assertTrue(Files.size(outputFile) > 0);

        try (InputStream in = Files.newInputStream(outputFile);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            binaryComprStrategy.decompress(in, out);

            assertEquals(testData, out.toString());
        }
    }

    @Test
    void compress_shouldThrowRuntimeExceptionOnIOException() throws IOException {
        when(mockInputStream.read(any(byte[].class))).thenThrow(new IOException("Simulated IO error"));

        assertThrows(RuntimeException.class, () -> {
            binaryComprStrategy.compress(mockInputStream, mockOutputStream);
        });
    }
}
