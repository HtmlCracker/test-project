package org.example.api.controllers;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.response.UploadFileResponseDto;
import org.example.api.entities.FileInfoEntity;
import org.example.api.exceptions.BadRequestException;
import org.example.api.factories.response.UploadFileResponseDtoFactory;
import org.example.api.services.StorageService;
import org.example.api.services.UploadProcessor;
import org.example.api.utils.FileUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;

@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
public class FileStorageController {
    UploadFileResponseDtoFactory uploadFileResponseDtoFactory;
    StorageService storageService;
    UploadProcessor uploadProcessor;

    public static final String UPLOAD_FILE = "api/private/file-storage/upload";

    @PostMapping(UPLOAD_FILE)
    public UploadFileResponseDto uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        System.out.println("File info - Name: " + file.getOriginalFilename() +
                ", Size: " + file.getSize() +
                ", ContentType: " + file.getContentType());

        if (file.isEmpty()) {
            throw new BadRequestException("File can't be empty");
        }
        FileInfoEntity entity = storageService.temporaryUploadFile(file);

        System.out.println(entity.toString());

        FileInfoEntity compressedEntity = uploadProcessor.compress(entity.getFilePath());

        System.out.println(compressedEntity.toString());

        return uploadFileResponseDtoFactory.makeUploadFileResponseDto(entity);
    }
}
