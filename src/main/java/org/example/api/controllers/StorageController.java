package org.example.api.controllers;

import org.example.api.dto.response.StorageDto;
import org.example.api.services.StorageService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
public class StorageController {
    StorageService storageService;

    public static final String CREATE_STORAGE = "api/private/storage/create/{profileId}";
    public static final String TEST = "api/private/storage/test";

    @PostMapping(TEST)
    public void test() {
        System.out.println("TEST");
    }

    @PostMapping(CREATE_STORAGE)
    public StorageDto createStorage(@PathVariable UUID profileId) {
        return storageService.createStorage(profileId);
    }
}
