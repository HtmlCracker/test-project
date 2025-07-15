package org.example.api.controllers;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.request.ProfileRegistrationRequestDto;
import org.example.api.dto.request.ProfileUpdateRequestDto;
import org.example.api.dto.response.DelProfileResponseDto;
import org.example.api.dto.response.ProfileResponseDto;
import org.example.api.entities.ProfileEntity;
import org.example.api.factories.ProfileDtoFactory;
import org.example.api.services.ProfileService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RestController
public class ProfileController {
    ProfileService profileService;

    ProfileDtoFactory profileDtoFactory;

    public static final String REGISTRATION_PROFILE = "api/public/accounts/registration";
    public static final String UPDATE_PROFILE = "api/private/accounts/update/{profileId}";
    public static final String GET_PROFILE = "api/public/accounts/get/{profileId}";
    public static final String DEL_PROFILE = "api/private/accounts/del/{profileId}";

    @PostMapping(REGISTRATION_PROFILE)
    public ProfileResponseDto registrationProfile(@Valid @RequestBody ProfileRegistrationRequestDto requestDto) {
        ProfileEntity profile = profileService.registerProfile(requestDto);
        return profileDtoFactory.makeProfileDto(profile);
    }

    @PostMapping(UPDATE_PROFILE)
    public ProfileResponseDto updateProfile(@PathVariable UUID profileId,
                                            @Valid @RequestBody ProfileUpdateRequestDto requestDto) {
        ProfileEntity savedProfileEntity = profileService.updateProfile(profileId, requestDto);
        return profileDtoFactory.makeProfileDto(savedProfileEntity);
    }

    @GetMapping(GET_PROFILE)
    public ProfileResponseDto getProfileDto(@PathVariable UUID profileId) {
        ProfileEntity profile = profileService.getProfileByIdOrThrowException(profileId);
        return profileDtoFactory.makeProfileDto(profile);
    }

    @DeleteMapping(DEL_PROFILE)
    public DelProfileResponseDto delProfile(@PathVariable UUID profileId) {
        return profileService.delProfile(profileId);
    }
}
