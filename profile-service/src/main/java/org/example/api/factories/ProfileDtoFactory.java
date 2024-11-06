package org.example.api.factories;

import org.example.api.dto.response.ProfileResponseDto;
import org.example.api.entities.ProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class ProfileDtoFactory {
    public ProfileResponseDto makeProfileDto(ProfileEntity entity) {
        return ProfileResponseDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .name(entity.getName())
                .surname(entity.getSurname())
                .description(entity.getDescription())
                .roles(entity.getRoles())
                .registrationDate(entity.getRegistrationDate())
                .build();
    }
}
