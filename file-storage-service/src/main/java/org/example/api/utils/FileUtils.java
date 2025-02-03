package org.example.api.utils;

import org.example.api.exceptions.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class FileUtils {
    String rootPath;

    public FileUtils(String rootPath) {
        this.rootPath = rootPath;
    }

    public static FileUtils of (String rootPath) {
        return new FileUtils(rootPath);
    }

    public void createDirectory(Path path, String directoryName) {
        Path newDirectoryPath = Paths.get(path.toString(), directoryName);
        File newDirectory = newDirectoryPath.toFile();

        if (newDirectory.exists())
            throw new BadRequestException(String.format("Directory with name %s already exists", directoryName));

        try {
            newDirectory.mkdir();
        } catch (SecurityException e) {
            throw new org.example.api.exceptions.IOException("Something went wrong");
        }
    }

    public void saveFileInByteToPath(Path path, String fileName, byte[] fileInByte) {
        Path pathToSave = Paths.get(path.toString(), fileName);

        try {
            Files.write(pathToSave, fileInByte, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new org.example.api.exceptions.IOException("The specified path does not exist or is inaccessible");
        }
    }

    public void saveMultipartFileToPath(Path path, String fileName, MultipartFile file) {
        Path pathToSave = Paths.get(path.toString(), fileName);
        File destination = pathToSave.toFile();

        try (FileOutputStream outputStream = new FileOutputStream(destination)) {
            outputStream.write(file.getBytes());
        } catch (IOException e) {
            throw new org.example.api.exceptions.IOException("The specified path does not exist or is inaccessible");
        }
    }

    public byte[] getFileByPath(Path path) {
        byte[] byteData;

        try {
            byteData = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new org.example.api.exceptions.IOException("File is corrupted.");
        }

        if (byteData.length == 0)
            throw new org.example.api.exceptions.IOException("File is corrupted.");

        return byteData;
    }
}
