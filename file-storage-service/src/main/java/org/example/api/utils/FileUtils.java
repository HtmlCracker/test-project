package org.example.api.utils;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FileUtils {
    public void mkDir(String pathName) {
        File theDir = new File(pathName);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
    }
}
