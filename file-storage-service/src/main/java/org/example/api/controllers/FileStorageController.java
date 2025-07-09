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
import org.example.api.repositories.FileInfoRepository;
import org.example.api.services.FileInfoCacheService;
import org.example.api.services.FileOperationService;
import org.example.api.services.FileStateMachineService;
import org.example.api.services.storage.TemporaryStorageService;
import org.example.api.statemachine.state.upload.UploadFileState;
import org.example.api.utils.FileUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
public class FileStorageController {
    FileInfoCacheService fileInfoCacheService;
    UploadFileResponseDtoFactory uploadFileResponseDtoFactory;
    TemporaryStorageService temporaryStorageService;
    FileStateMachineService fileProcessorService;
    FileOperationService fileStorageService;
    FileUtils fileUtils;

    public static final String UPLOAD_FILE = "api/private/file-storage/upload";
    public static final String DELETE_FILE = "api/private/file-storage/delete/{fileId}";
    public static final String GET_FILE = "api/private/file-storage/get/{fileId}";

    @PostMapping(UPLOAD_FILE)
    public UploadFileResponseDto uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File can't be empty");
        }
        FileInfoEntity entity = temporaryStorageService.temporaryUploadFile(file);
        fileProcessorService.uploadFile(entity.getId().toString());
        FileInfoEntity fileInfoEntity = fileInfoCacheService.getFileEntityById(entity.getId());

        return uploadFileResponseDtoFactory.makeUploadFileResponseDto(fileInfoEntity);
    }

    @DeleteMapping(DELETE_FILE)
    public DeleteFileResponseDto deleteFile(@PathVariable UUID fileId) {
        return fileStorageService.delete(fileId);
    }

    @GetMapping(value = GET_FILE, produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StreamingResponseBody> getFile(@PathVariable UUID fileId) throws FileNotFoundException {
        FileInfoEntity fileInfoEntity = fileInfoCacheService.getFileEntityById(fileId);
        String originalFileName = fileInfoEntity.getOriginalFileName();
        String path = fileProcessorService.getFile(fileInfoEntity.getFilePath());

        File file = fileUtils.getFileOrThrowException(path);

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file)) {
            @Override
            public String getFilename() {
                return originalFileName;
            }
        };

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream in = new FileInputStream(file)) {
                in.transferTo(outputStream);
            } finally {
                fileUtils.deleteFile(path);
            }
        };
        String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
        headers.add("X-File-Name", encodedFileName);
        headers.add("X-File-Size", String.valueOf(file.length()));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }
}
