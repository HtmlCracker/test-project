package org.example.api.utils;

import org.example.api.exceptions.BadRequestException;
import org.example.api.exceptions.FileNotFoundException;
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
        try {
            return createFileInDir(fileName, file.getBytes(), pathToDir);
        } catch (IOException e) {
            throw new BadRequestException("File can't be empty");
        }
    }

    public String createFileInDir(String fileName, byte[] fileInByte, String pathToDir) {
        if (fileInByte.length == 0) {
            throw new BadRequestException("File can't be empty");
        }
        createDirectoryIfNotExists(pathToDir);

        Path filePath = Paths.get(pathToDir, fileName);
        File dest = new File(filePath.toString());

        try {
            Files.write(filePath, fileInByte);
            return filePath.toString();
        } catch (IOException e) {
            throw new BadRequestException("Save file error");
        }
    }

    public void deleteFile(String path) throws IOException {
        getFileOrThrowException(path);
        Files.delete(Paths.get(path));
    }

    public void createDirectoryIfNotExists(String pathToDir) {
        Path dirPath = Paths.get(pathToDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new BadRequestException("Failed to create directory: " + dirPath);
        }
    }

    public String calculateUniqueFileHash(MultipartFile file) {
        String baseHash = calculateFileHash(file);
        String timestamp = String.valueOf(System.currentTimeMillis());
        return baseHash + timestamp;
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

    public String getFileExtension(String fileName) {
        if (fileName.isEmpty()) {
            throw new BadRequestException("File name can't be empty");
        }
        System.out.println(fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()));
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    public String getFileMime(File file) {
        try {
            String mimeType = Files.probeContentType(file.toPath());

            if (mimeType == null) {
                return "unknown";
            }

            return mimeType.split("/")[0];
        } catch (IOException e) {
            return "unknown";
        }
    }

    public String getFileName(String path) {
        return Paths.get(path).getFileName().toString();
    }

    public File getFileOrThrowException(String path) {
        File file = new File(path);
        throwExceptionIfFileIsNotExists(file);

        return file;
    }

    private void throwExceptionIfFileIsNotExists(File file) {
        if (!file.exists() && !file.isDirectory()) {
            throw new FileNotFoundException("File is not exists");
        }
    }
}