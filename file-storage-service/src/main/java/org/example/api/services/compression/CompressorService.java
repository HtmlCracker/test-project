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
import org.example.api.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class CompressorService {
    final FileTypeConfig fileTypeConfig;
    final FileUtils fileUtils;
    final Map<String, ComprssionStrategy> compressionStrategies = new HashMap<>();

    @Value("${PATH_TO_COMPRESSED_STORAGE}")
    String compressedStoragePath;

    @PostConstruct
    public void initCompressionStrategies() {
        ComprssionStrategy textComprStrategy = new TextComprStrategy();
        String textType = fileTypeConfig.getTextType();
        compressionStrategies.putIfAbsent(textType, textComprStrategy);

        ComprssionStrategy binaryComprStrategy = new BinaryComprStrategy();
        String binaryType = fileTypeConfig.getBinaryType();
        compressionStrategies.putIfAbsent(binaryType, binaryComprStrategy);
    }

    public CompressedFileDto compressFileAndWrite(String path) {
        File file = fileUtils.getFileOrThrowException(path);
        String fileMimeType = fileUtils.getFileMime(file);
        System.out.println(fileMimeType + " +++++++");
        ComprssionStrategy compressionStrategy = compressionStrategies.get(fileMimeType);

        byte[] compressedByte = compressFile(file, compressionStrategy);
        String pathToCompressedFile = writeFile(compressedByte, path, compressionStrategy);

        return CompressedFileDto.builder()
                .path(pathToCompressedFile)
                .compressedSize((long) compressedByte.length)
                .build();
    }

    private byte[] compressFile(File file,
                                ComprssionStrategy compressionStrategy) {
        try {
            InputStream fileInputStream = new FileInputStream(file);
            return compressionStrategy.compress(fileInputStream);
        } catch (FileNotFoundException e) {
            throw new BadRequestException("File not found.");
        }
    }

    private String writeFile(byte[] compressedByte,
                             String oldPath,
                             ComprssionStrategy compressionStrategy) {
        String fileExc = compressionStrategy.getCompressedFileExtension();
        String fileName = fileUtils.getFileName(oldPath) + fileExc;

        return fileUtils.createFileInDir(fileName, compressedByte, compressedStoragePath);
    }
}
