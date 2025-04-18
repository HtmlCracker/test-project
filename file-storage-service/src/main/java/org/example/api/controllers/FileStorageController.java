package org.example.api.controllers;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.response.DeleteFileResponseDto;
import org.example.api.dto.response.UploadFileResponseDto;
import org.example.api.entities.FileInfoEntity;
import org.example.api.exceptions.BadRequestException;
import org.example.api.factories.response.UploadFileResponseDtoFactory;
import org.example.api.services.FileInfoCacheService;
import org.example.api.services.FileProcessorService;
import org.example.api.services.FileStorageService;
import org.example.api.services.storage.TemporaryStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
public class FileStorageController {
    FileInfoCacheService fileInfoCacheService;
    UploadFileResponseDtoFactory uploadFileResponseDtoFactory;
    TemporaryStorageService temporaryStorageService;
    FileProcessorService fileProcessorService;
    FileStorageService fileStorageService;

    public static final String UPLOAD_FILE = "api/private/file-storage/upload";
    public static final String DELETE_FILE = "api/private/file-storage/delete/{fileId}";

    @PostMapping(UPLOAD_FILE)
    public UploadFileResponseDto uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File can't be empty");
        }
        FileInfoEntity entity = temporaryStorageService.temporaryUploadFile(file);
        fileProcessorService.processFile(entity.getId().toString());
        FileInfoEntity fileInfoEntity = fileInfoCacheService.getFileEntityById(entity.getId());
        return uploadFileResponseDtoFactory.makeUploadFileResponseDto(fileInfoEntity);
    }

    @DeleteMapping(DELETE_FILE)
    public DeleteFileResponseDto deleteFile(@PathVariable UUID fileId) {
        return fileStorageService.delete(fileId);
    }
}
