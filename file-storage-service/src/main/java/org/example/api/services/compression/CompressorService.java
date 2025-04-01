package org.example.api.services.compression;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.configs.FileTypeConfig;
import org.example.api.exceptions.BadRequestException;
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
import java.util.prefs.BackingStoreException;

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
        System.out.println("iniiiiiiit");
        fileTypeConfig.getTextTypes().forEach(type -> {
            if(!compressionStrategies.containsKey(type)) {
                compressionStrategies.put(type, new TextComprStrategy());
            }
        });
    }

    public void compressFileAndWrite(String path) {
        File file = fileUtils.getFileOrThrowException(path);
        String fileType = fileUtils.getFileExtension(path);

        System.out.println("Wot: " + fileType);

        byte[] compressedByte = compressFile(file, fileType);
        String pathToCompressedFile = writeFile(compressedByte, path, fileType);

        System.out.println(pathToCompressedFile);
    }

    private byte[] compressFile(File file, String fileType) {
        System.out.println(fileType);
        ComprssionStrategy compressionStrategy = compressionStrategies.get(fileType);
        System.out.println(compressionStrategy.getCompressedFileExtension());
        try {
            InputStream fileInputStream = new FileInputStream(file);
            return compressionStrategy.compress(fileInputStream);
        } catch (Exception e) {
            System.out.println("aaa");
            throw new BadRequestException("aaa");
        }
    }

    private String writeFile(byte[] compressedByte, String path, String fileType) {
        ComprssionStrategy compressionStrategy = compressionStrategies.get(fileType);
        String fileExc = compressionStrategy.getCompressedFileExtension();
        String fileName = fileUtils.getFileName(path) + fileExc;
        String directoryPath = path.substring(0, path.lastIndexOf('/') + 1);

        return fileUtils.createFileInDir(fileName, compressedByte, directoryPath);
    }
}
