package org.example.api.controllers;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.api.dto.request.DeleteProjectProfileRequestDto;
import org.example.api.dto.request.ProjectProfileRequestDto;
import org.example.api.dto.response.DeleteProjectProfileResponseDto;
import org.example.api.dto.response.ProjectProfileResponseDto;
import org.example.api.entities.ProjectProfileEntity;
import org.example.api.factories.ProjectProfileResponseDtoFactory;
import org.example.api.services.ProjectProfileService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@Slf4j
public class ProjectProfileController {
    ProjectProfileService profileService;
    ProjectProfileResponseDtoFactory projectProfileResponseDtoFactory;

    public static final String REGISTRATION_PROJECT_PROFILE = "api/private/projects/registration";
    public static final String UPDATE_PROJECT_PROFILE = "api/private/projects/update/{projectProfileId}";
    public static final String DELETE_PROJECT_PROFILE = "api/private/projects/delete";
    public static final String GET_PROJECT_PROFILE = "api/private/projects/get/{projectProfileId}";

    @PostMapping(REGISTRATION_PROJECT_PROFILE)
    public ProjectProfileResponseDto registrationProjectProfile(@Valid @RequestBody ProjectProfileRequestDto dto) {
        log.info("Starting project profile registration, with request: {}", dto);
        ProjectProfileEntity savedProfileEntity = profileService.saveProjectProfileEntity(dto);
        log.info("Profile saved successfully. Profile ID: {}", savedProfileEntity.getId());
        return projectProfileResponseDtoFactory.makeDto(savedProfileEntity);
    }

    @PutMapping(UPDATE_PROJECT_PROFILE)
    public ProjectProfileResponseDto updateProjectProfile(@PathVariable UUID projectProfileId,
                                                          @Valid @RequestBody ProjectProfileRequestDto dto) {
        log.info("Starting project profile update, with request: {}", dto);
        ProjectProfileEntity savedProfileEntity = profileService.updateProjectProfile(
                projectProfileId, dto
        );
        log.info("Profile updated successfully. Profile ID: {}", savedProfileEntity.getId());
        return projectProfileResponseDtoFactory.makeDto(savedProfileEntity);
    }

    @DeleteMapping(DELETE_PROJECT_PROFILE)
    public DeleteProjectProfileResponseDto deleteProjectProfile(
            @Valid @RequestBody DeleteProjectProfileRequestDto dto) {
        log.info("Starting project profile delete, with request: {}", dto);
        return profileService.deleteProjectProfile(dto);
    }

    @GetMapping(GET_PROJECT_PROFILE)
    public ProjectProfileResponseDto getProjectProfile(@PathVariable UUID projectProfileId) {
        log.info("Starting project profile get, with projectProfileId: {}", projectProfileId);
        ProjectProfileEntity entity = profileService.getProjectProfile(projectProfileId);
        log.info("Profile got successfully. Profile ID: {}", projectProfileId);
        return projectProfileResponseDtoFactory.makeDto(entity);
    }
}
