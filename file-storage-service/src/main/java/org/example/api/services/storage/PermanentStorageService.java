package org.example.api.services.storage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.service.PermanentFileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PermanentStorageService {
    @Value("${PATH_TO_PERMANENT_STORAGE}")
    private String permanentStoragePath;

    public PermanentFileDto permanentUploadFile() {
        return PermanentFileDto.builder()
                .path("")
                .fileSize((long) 1)
                .build();
    }
}
