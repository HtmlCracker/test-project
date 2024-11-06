package org.example.api.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.request.ProfileRegistrationRequestDto;
import org.example.api.entities.ProfileEntity;
import org.example.api.exceptions.BadRequestException;
import org.example.api.repositories.ProfileRepository;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProfileRegistrationService {
    ProfileRepository profileRepository;

    public ProfileEntity registerProfile(ProfileRegistrationRequestDto dto) {

        ProfileEntity profileEntity = buildProfileEntityOrThrowException(dto);

        return profileRepository.save(profileEntity);
    }

    private ProfileEntity buildProfileEntityOrThrowException(ProfileRegistrationRequestDto dto) {
        throwExceptionIfProfileWithEmailAlreadyExists(dto.getEmail());

        return ProfileEntity.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .surname(dto.getSurname())
                .description(dto.getDescription())
                .roles(dto.getRoles())
                .build();
    }

    private void throwExceptionIfProfileWithEmailAlreadyExists(String email) {
        profileRepository.findByEmail(email)
                .ifPresent((profile -> {
                    throw new BadRequestException(String.format("Account with email \"%s\" already exists.", email));
                }));
    }
}
