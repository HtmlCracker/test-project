package org.example.api.controllers;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.response.UploadFileResponseDto;
import org.example.api.entities.FileInfoEntity;
import org.example.api.factories.response.UploadFileResponseDtoFactory;
import org.example.api.services.StorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
public class FileStorageController {
    UploadFileResponseDtoFactory uploadFileResponseDtoFactory;
    StorageService storageService;

    public static final String UPLOAD_FILE = "api/private/file-storage/upload";

    @PostMapping(UPLOAD_FILE)
    public UploadFileResponseDto uploadFile(@RequestParam("file") MultipartFile file) {
        FileInfoEntity entity = storageService.temporaryUploadFile(file);
        return uploadFileResponseDtoFactory.makeUploadFileResponseDto(entity);
    }
}
