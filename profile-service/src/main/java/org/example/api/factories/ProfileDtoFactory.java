package org.example.api.factories;

import org.example.api.dto.ProfileDto;
import org.example.api.entities.ProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class ProfileDtoFactory {
    public ProfileDto makeProfileDto(ProfileEntity entity) {
        return ProfileDto.builder()
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
