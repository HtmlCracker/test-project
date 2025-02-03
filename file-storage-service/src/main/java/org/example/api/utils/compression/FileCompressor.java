package org.example.api.utils.compression;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.example.api.exceptions.IllegalArgumentException;
import org.example.api.utils.compression.implementations.TextCompressorStrategy;
import org.example.api.utils.compression.interfaces.CompressionStrategy;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileCompressor {
    final Map<String, CompressionStrategy> compressionStrategies = Map.of(
            "txt", new TextCompressorStrategy()
    );

    public byte[] compressFile(MultipartFile file) {
        String fileType = getFileType(file);
        byte[] fileBytes;
        CompressionStrategy compressionStrategy = compressionStrategies.get(fileType);

        if (compressionStrategy == null) {
            return new byte[0];
        }

        fileBytes = getBytesByMultipartFile(file);
        return compressionStrategy.compress(fileBytes);
    }

    private String getFileType(MultipartFile file) {
        String filename = file.getOriginalFilename();

        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        int lastDotIndex = filename.lastIndexOf('.');

        if (lastDotIndex == -1 || lastDotIndex == 0 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    private byte[] getBytesByMultipartFile(MultipartFile file) {
        byte[] fileBytes;

        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new org.example.api.exceptions.IOException("File is corrupted");
        }

        return fileBytes;
    }

    public void testDecompressFile(Path compressedFilePath) {
        try (InputStream fileInputStream = Files.newInputStream(compressedFilePath);
             InputStream gzipInputStream = new GzipCompressorInputStream(fileInputStream);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("File decompressed successfully. Size: " + outputStream.size());
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error decompressing file: " + e.getMessage(), e);
        }
    }
}
