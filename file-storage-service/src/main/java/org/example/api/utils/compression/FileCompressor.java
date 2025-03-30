package org.example.api.utils.compression;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.example.api.configs.FileTypesConfig;
import org.example.api.exceptions.IllegalArgumentException;
import org.example.api.utils.compression.implementations.GenericCompressorStrategy;
import org.example.api.utils.compression.implementations.TextCompressorStrategy;
import org.example.api.utils.compression.interfaces.CompressionStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileCompressor {
    final FileTypesConfig fileTypesConfig;
    final GenericCompressorStrategy genericCompressorStrategy = new GenericCompressorStrategy();

    final Map<String, CompressionStrategy> compressionStrategies = new HashMap<>();

    @PostConstruct
    public void initCompressionStrategies() {
        fileTypesConfig.getTextTypes().forEach(type -> {
            if(!compressionStrategies.containsKey(type)) {
                compressionStrategies.put(type, new TextCompressorStrategy());
            }
        });
    }

    public byte[] compressFile(MultipartFile file) {
        String fileType = getFileType(file);
        byte[] fileBytes;
        CompressionStrategy compressionStrategy = compressionStrategies.get(fileType);

        fileBytes = getBytesByMultipartFile(file);

        if (compressionStrategy == null) {
            return genericCompressorStrategy.compress(fileBytes);
        }

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
