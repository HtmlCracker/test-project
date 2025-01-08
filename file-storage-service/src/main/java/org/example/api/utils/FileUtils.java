package org.example.api.utils;

import org.example.api.exceptions.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class FileUtils {
    String rootPath;

    public FileUtils(String rootPath) {
        this.rootPath = rootPath;
    }

    public static FileUtils of (String rootPath) {
        return new FileUtils(rootPath);
    }

    public void createDirectory(String directoryName) {
        String newDirectoryPath = rootPath + File.separator + directoryName;
        File newDirectory = new File(newDirectoryPath);

        if (newDirectory.exists())
            throw new BadRequestException(String.format("Directory with name %s already exists", directoryName));

        try {
            newDirectory.mkdir();
        } catch (SecurityException e) {
            throw new org.example.api.exceptions.IOException("Something went wrong");
        }
    }

    public void addFileToPath(String path, String fileName, MultipartFile file) {
        String pathToSave = rootPath + "/" + path + "/" + fileName;
        File destination = new File(pathToSave);

        try (FileOutputStream outputStream = new FileOutputStream(destination)) {
            outputStream.write(file.getBytes());
        } catch (IOException e) {
            throw new org.example.api.exceptions.IOException("The specified path does not exist or is inaccessible");
        }
    }
}
