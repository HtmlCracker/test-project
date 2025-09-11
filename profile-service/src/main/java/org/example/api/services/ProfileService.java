package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.api.dto.request.ProfileRegistrationRequestDto;
import org.example.api.dto.request.ProfileUpdateRequestDto;
import org.example.api.dto.response.DelProfileResponseDto;
import org.example.api.entities.ProfileEntity;
import org.example.api.exceptions.BadRequestException;
import org.example.api.exceptions.NotFoundException;
import org.example.api.repositories.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    ProfileRepository profileRepository;

    public ProfileEntity registerProfile(UUID userId, ProfileRegistrationRequestDto dto) {
        throwExceptionIfProfileWithIdAlreadyExists(userId);
        ProfileEntity profileEntity = buildProfileEntity(userId, dto);
        return profileRepository.save(profileEntity);
    }

    private void throwExceptionIfProfileWithIdAlreadyExists(UUID id) {
        profileRepository.findById(id)
                .ifPresent((profile -> {
                    log.warn("Attempt to create profile with existing ID: {}", id);
                    throw new BadRequestException(String.format("Account with id \"%s\" already exists.", id));
                }));
    }

    private ProfileEntity buildProfileEntity(UUID id, ProfileRegistrationRequestDto dto) {
        return ProfileEntity.builder()
                .id(id)
                .name(dto.getName())
                .surname(dto.getSurname())
                .roles(getRolesListByString(dto.getRoles()))
                .build();
    }

    private List<String> getRolesListByString(String rolesString) {
        if (rolesString.trim().isEmpty()) {
            log.debug("Roles string is empty, returning empty list");
            return Collections.emptyList();
        }
        List<String> roles = Arrays.asList(rolesString.split(","));
        log.debug("Parsed {} roles", roles.size());
        return roles;
    }

    public ProfileEntity updateProfile(UUID profileId, ProfileUpdateRequestDto dto) {
        ProfileEntity existingProfileEntity = getProfileByIdOrThrowException(profileId);
        ProfileEntity updatedProfileEntity = mergeProfileUpdatesIntoEntity(existingProfileEntity, dto);
        return profileRepository.save(updatedProfileEntity);
    }

    public ProfileEntity getProfileByIdOrThrowException(UUID id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Attempt to get profile with does not exist if: {}", id);
                    return new NotFoundException(String.format("Account with id \"%s\" does not exist", id));
                });
    }

    private ProfileEntity mergeProfileUpdatesIntoEntity(ProfileEntity profileEntity,
                                              ProfileUpdateRequestDto updatedDto) {
        profileEntity.setName(updatedDto.getName());
        profileEntity.setSurname(updatedDto.getSurname());
        profileEntity.setDescription(updatedDto.getDescription());
        profileEntity.setRoles(getRolesListByString(updatedDto.getRoles()));

        return profileEntity;
    }

    public DelProfileResponseDto delProfile(UUID profileId) {
        ProfileEntity profile = getProfileByIdOrThrowException(profileId);

        profileRepository.delete(profile);

        return DelProfileResponseDto.builder()
                .status("The profile has been deleted.")
                .build();
    }
}
