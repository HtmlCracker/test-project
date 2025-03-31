package org.example.api.utils;

import org.example.api.exceptions.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class FileUtils {
    public String createFileInDir(String fileName, MultipartFile file, String pathToDir) {
        throwExceptionIfFileIsNull(file);
        createDirectoryIfNotExists(pathToDir);

        Path filePath = Paths.get(pathToDir, fileName);
        File dest = new File(filePath.toString());

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BadRequestException("Save file error");
        }

        return filePath.toString();
    }

    private void throwExceptionIfFileIsNull(MultipartFile file) {
        if (file.isEmpty())
            throw new BadRequestException("File can't be empty");
    }

    public void createDirectoryIfNotExists(String pathToDir) {
        Path dirPath = Paths.get(pathToDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new BadRequestException("Failed to create directory: " + dirPath);
        }
    }

    public String calculateFileHash(MultipartFile file) {
        try {
            byte[] fileBytes = file.getBytes();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileBytes);
            return bytesToHex(hashBytes);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate file hash", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
