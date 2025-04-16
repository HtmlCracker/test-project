package org.example.api.controllers;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.response.UploadFileResponseDto;
import org.example.api.entities.FileInfoEntity;
import org.example.api.exceptions.BadRequestException;
import org.example.api.exceptions.NotFoundException;
import org.example.api.factories.response.UploadFileResponseDtoFactory;
import org.example.api.repositories.FileInfoRepository;
import org.example.api.services.FileProcessorService;
import org.example.api.services.storage.TemporaryStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
public class FileStorageController {
    FileInfoRepository fileInfoRepository;
    UploadFileResponseDtoFactory uploadFileResponseDtoFactory;
    TemporaryStorageService storageService;
    FileProcessorService fileProcessorService;

    public static final String UPLOAD_FILE = "api/private/file-storage/upload";

    @PostMapping(UPLOAD_FILE)
    public UploadFileResponseDto uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File can't be empty");
        }
        FileInfoEntity entity = storageService.temporaryUploadFile(file);

        fileProcessorService.processFile(entity.getId().toString());

        FileInfoEntity fileInfoEntity = fileInfoRepository.findById(entity.getId())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Entity with id: %s is not exists", entity.getId().toString()))
                );

        return uploadFileResponseDtoFactory.makeUploadFileResponseDto(fileInfoEntity);
    }
}
