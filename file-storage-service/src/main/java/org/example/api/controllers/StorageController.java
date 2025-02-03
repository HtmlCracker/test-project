package org.example.api.controllers;

import org.example.api.dto.request.UploadFileRequestDto;
import org.example.api.dto.response.GetFileResponseDto;
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

    public static final String UPLOAD_FILE = "api/private/storage/upload";
    public static final String GET_FILE = "api/private/storage/get/{fileId}";
    public static final String DEL_FILE = "api/private/storage/delete/{fileId}";

    /*@PostMapping(UPLOAD_FILE)
    public UploadFileResponseDto uploadFile(@RequestParam(value = "file") MultipartFile file,
                                            @RequestParam(value = "isCompressed") boolean isCompressed) {
        return storageService.uploadFile(file, isCompressed);
    }*/

    //@GetMapping(GET_FILE)
    //public GetFileResponseDto getFile(@PathVariable UUID fileId) {
    //    return storageService.getFile(fileId);
    //}

    // TODO: Сделать
    @DeleteMapping(DEL_FILE)
    public void delFile(@PathVariable UUID fileId) {
    }
}
