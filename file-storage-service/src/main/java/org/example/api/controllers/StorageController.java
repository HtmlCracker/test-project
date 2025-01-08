package org.example.api.controllers;

import org.example.api.dto.request.UploadFileRequestDto;
import org.example.api.dto.response.StorageDto;
import org.example.api.dto.response.UploadFileResponseDto;
import org.example.api.services.StorageService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
public class StorageController {
    StorageService storageService;

    public static final String CREATE_STORAGE = "api/private/storage/create/{profileId}";
    public static final String UPLOAD_FILE = "api/private/storage/upload";

    @PostMapping(CREATE_STORAGE)
    public StorageDto createStorage(@PathVariable String profileId) {
        return storageService.createStorage(profileId);
    }

    @PostMapping(UPLOAD_FILE)
    public UploadFileResponseDto uploadFile(@RequestParam("ownerId") String ownerId,
                                            @RequestParam(value = "file") MultipartFile file) {
        return storageService.uploadFile(ownerId, file);
    }
}
