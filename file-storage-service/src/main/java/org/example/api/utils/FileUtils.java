package org.example.api.utils;

import org.example.api.exceptions.BadRequestException;
import org.example.api.exceptions.FileNotFoundException;
import org.example.api.exceptions.FileProcessingException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class FileUtils {
    public String createFileInDir(String fileName, MultipartFile file, String pathToDir) {
        try {
            return createFileInDir(fileName, file.getInputStream(), pathToDir);
        } catch (IOException e) {
            throw new BadRequestException("File can't be empty or inaccessible");
        }
    }

    public String createFileInDir(String fileName, File file, String pathToDir) {
        try {
            return createFileInDir(fileName, new FileInputStream(file), pathToDir);
        } catch (IOException e) {
            throw new BadRequestException("File can't be empty or inaccessible");
        }
    }

    public String createFileInDir(String fileName, InputStream inputStream, String pathToDir) {
        createDirectoryIfNotExists(pathToDir);
        Path filePath = Paths.get(pathToDir, fileName);

        try (inputStream) {
            if (inputStream == null) {
                throw new BadRequestException("Input stream is null");
            }

            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            throw new BadRequestException("Save file error: " + e.getMessage());
        }
    }

    public String moveFileTo(String sourcePath, String destinationPath) {
        try {
            Path source = Paths.get(sourcePath);
            Path destination = Paths.get(destinationPath);

            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            return destination.toAbsolutePath().toString();
        } catch (FileAlreadyExistsException e) {
            throw new FileProcessingException("File already exists");
        } catch (IOException e) {
            throw new FileProcessingException("IOException");
        }
    }

    public void deleteFile(String path) {
        getFileOrThrowException(path);
        try {
            Files.delete(Paths.get(path));
        } catch (IOException e) {
            throw new FileProcessingException(
                    String.format("Error processing file with path: %s", path)
            );
        }
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
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    public String getFileMime(String path) {
        File file = getFileOrThrowException(path);
        return getFileMime(file);
    }

    public String getFileMime(File file) {
        try {
            String mimeType = Files.probeContentType(file.toPath());

            if (mimeType == null) {
                return "unknown mime type";
            }

            return mimeType.split("/")[0];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFileMime(MultipartFile file) {
        String mimeType = file.getContentType();

        if (mimeType == null) {
            return "unknown mime type";
        }

        return mimeType.split("/")[0];
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