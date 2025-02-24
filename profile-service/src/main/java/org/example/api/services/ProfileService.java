package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
public class ProfileService {
    ProfileRepository profileRepository;


    public ProfileEntity registerProfile(ProfileRegistrationRequestDto dto) {
        throwExceptionIfProfileWithEmailAlreadyExists(dto.getEmail());
        ProfileEntity profileEntity = buildProfileEntity(dto);
        return profileRepository.save(profileEntity);
    }

    private void throwExceptionIfProfileWithEmailAlreadyExists(String email) {
        profileRepository.findByEmail(email)
                .ifPresent((profile -> {
                    throw new BadRequestException(String.format("Account with email \"%s\" already exists.", email));
                }));
    }

    private ProfileEntity buildProfileEntity(ProfileRegistrationRequestDto dto) {
        return ProfileEntity.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .surname(dto.getSurname())
                .description(dto.getDescription())
                .roles(getRolesListByString(dto.getRoles()))
                .build();
    }

    private List<String> getRolesListByString(String rolesString) {
        if (rolesString.trim().isEmpty())
            return Collections.emptyList();

        return Arrays.asList(rolesString.split(","));
    }


    public ProfileEntity updateProfile(UUID profileId, ProfileUpdateRequestDto dto) {
        ProfileEntity existingProfileEntity = getProfileByIdOrThrowException(profileId);
        ProfileEntity updatedProfileEntity = mergeProfileUpdatesIntoEntity(existingProfileEntity, dto);
        return profileRepository.save(updatedProfileEntity);
    }

    public ProfileEntity getProfileByIdOrThrowException(UUID id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Account with id \"%s\" does not exist", id)));
    }

    private ProfileEntity mergeProfileUpdatesIntoEntity(ProfileEntity profileEntity,
                                              ProfileUpdateRequestDto updatedDto) {
        profileEntity.setEmail(updatedDto.getEmail());
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
