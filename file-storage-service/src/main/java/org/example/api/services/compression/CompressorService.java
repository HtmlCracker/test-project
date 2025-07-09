package org.example.api.services.compression;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.configs.FileTypeConfig;
import org.example.api.dto.service.CompressedFileDto;
import org.example.api.exceptions.BadRequestException;
import org.example.api.services.compression.impl.BinaryComprStrategy;
import org.example.api.services.compression.impl.TextComprStrategy;
import org.example.api.services.compression.interfaces.ComprssionStrategy;
import org.example.api.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class CompressorService {
    final FileTypeConfig fileTypeConfig;
    final FileUtils fileUtils;
    final Map<String, ComprssionStrategy> compressionStrategies = new HashMap<>();
    final Map<String, ComprssionStrategy> decompressionStrategies = new HashMap<>();

    @Value("${PATH_TO_COMPRESSED_STORAGE}")
    String compressedStoragePath;

    @Value("${PATH_TO_DECOMPRESSED_STORAGE}")
    String decompressedStoragePath;

    @PostConstruct
    public void initCompressionStrategies() {
        ComprssionStrategy textComprStrategy = new TextComprStrategy();
        String textType = fileTypeConfig.getTextType();
        compressionStrategies.putIfAbsent(textType, textComprStrategy);
        decompressionStrategies.putIfAbsent(textComprStrategy.getCompressedFileExtension(), textComprStrategy);

        ComprssionStrategy binaryComprStrategy = new BinaryComprStrategy();
        String binaryType = fileTypeConfig.getBinaryType();
        compressionStrategies.putIfAbsent(binaryType, binaryComprStrategy);
        decompressionStrategies.putIfAbsent(binaryComprStrategy.getCompressedFileExtension(), binaryComprStrategy);
    }

    public CompressedFileDto compressFileAndWrite(String path) {
        File file = fileUtils.getFileOrThrowException(path);
        String fileMimeType = fileUtils.getFileMime(file);
        ComprssionStrategy compressionStrategy = compressionStrategies.get(fileMimeType);
        String fileExc = compressionStrategy.getCompressedFileExtension();
        File compressedFile = new File(compressedStoragePath, file.getName()+fileExc);

        long compressedSize = compressFile(file, compressionStrategy, compressedFile);

        return CompressedFileDto.builder()
                .path(compressedFile.getPath())
                .compressedSize(compressedSize)
                .build();
    }

    public String decompressFileAndWrite(String path) {
        File inputFile = fileUtils.getFileOrThrowException(path);
        String fileExtension = "." + fileUtils.getFileExtension(path);
        ComprssionStrategy comprssionStrategy = decompressionStrategies.get(fileExtension);

        String fileName = fileUtils.getFileName(path);
        String baseName = fileName.replaceFirst("\\.[^.]+$", "");
        File outputFile = new File(decompressedStoragePath, baseName);

        return decompressFile(inputFile, comprssionStrategy, outputFile);
    }

    private long compressFile(File file, ComprssionStrategy compressionStrategy, File outputFile) {
        try (InputStream fileInputStream = new BufferedInputStream(new FileInputStream(file), 8 * 1024 * 1024);
                OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(outputFile), 8 * 1024 * 1024)) {
            compressionStrategy.compress(fileInputStream, fileOutputStream);
            return outputFile.length();
        } catch (IOException e) {
            throw new BadRequestException("File compression failed");
        }
    }

    private String decompressFile(File file,
                                  ComprssionStrategy compressionStrategy,
                                  File outputFile) {
        try (InputStream fileInputStream = new FileInputStream(file);
                OutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            compressionStrategy.decompress(fileInputStream, fileOutputStream);
            return outputFile.getPath();
        } catch (FileNotFoundException e) {
            throw new BadRequestException("File not found.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
