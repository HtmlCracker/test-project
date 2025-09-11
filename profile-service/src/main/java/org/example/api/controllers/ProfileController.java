package org.example.api.controllers;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.api.dto.request.ProfileRegistrationRequestDto;
import org.example.api.dto.request.ProfileUpdateRequestDto;
import org.example.api.dto.response.DelProfileResponseDto;
import org.example.api.dto.response.ProfileResponseDto;
import org.example.api.entities.ProfileEntity;
import org.example.api.factories.ProfileDtoFactory;
import org.example.api.services.ProfileService;
import org.example.api.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
@Slf4j
public class ProfileController {
    ProfileService profileService;
    ProfileDtoFactory profileDtoFactory;
    JwtUtils jwtUtils;

    public static final String REGISTRATION_PROFILE = "api/public/accounts/registration";
    public static final String UPDATE_PROFILE = "api/private/accounts/update/{profileId}";
    public static final String GET_PROFILE = "api/private/accounts/get/{profileId}";

    @PostMapping(REGISTRATION_PROFILE)
    public ProfileResponseDto registrationProfile(
            @Valid @RequestBody ProfileRegistrationRequestDto requestDto,
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("Starting profile registration for request: {}", requestDto);

        String jwt = authHeader.substring(7);
        UUID userId = jwtUtils.extractUserId(jwt);
        ProfileEntity profile = profileService.registerProfile(userId, requestDto);
        log.info("Profile registered successfully. Profile ID: {}",
                profile.getId());

        return profileDtoFactory.makeProfileDto(profile);
    }

    @PostMapping(UPDATE_PROFILE)
    public ProfileResponseDto updateProfile(@PathVariable UUID profileId,
                                            @Valid @RequestBody ProfileUpdateRequestDto requestDto) {
        log.info("Starting profile registration for profile with id: {}, with request: {}",
                profileId, requestDto);
        ProfileEntity savedProfileEntity = profileService.updateProfile(profileId, requestDto);
        log.info("Profile updated successfully. Profile ID: {}",
                savedProfileEntity.getId());
        return profileDtoFactory.makeProfileDto(savedProfileEntity);
    }

    @GetMapping(GET_PROFILE)
    public ProfileResponseDto getProfileDto(@PathVariable UUID profileId) {
        log.info("Starting profile registration for profile with id: {}",
                profileId);
        ProfileEntity profile = profileService.getProfileByIdOrThrowException(profileId);
        log.info("Profile got successfully. Profile ID: {}",
                profileId);
        return profileDtoFactory.makeProfileDto(profile);
    }
}
