package org.example.api.factories;

import org.example.api.dto.response.ProfileResponseDto;
import org.example.api.entities.ProfileEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProfileDtoFactory {
    public ProfileResponseDto makeProfileDto(ProfileEntity entity) {
        return ProfileResponseDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .birthDate(entity.getBirthDate())
                .surname(entity.getSurname())
                .description(entity.getDescription())
                .roles(entity.getRoles())
                .registrationDate(entity.getRegistrationDate())
                .build();
    }

    public List<ProfileResponseDto> makeProfileDtoList(List<ProfileEntity> profiles) {
        List<ProfileResponseDto> list =  new ArrayList<>();
        for(ProfileEntity  profile : profiles) {
            list.add(ProfileResponseDto.builder()
                    .id(profile.getId())
                    .email(profile.getEmail())
                    .name(profile.getName())
                    .phoneNumber(profile.getPhoneNumber())
                    .birthDate(profile.getBirthDate())
                    .surname(profile.getSurname())
                    .description(profile.getDescription())
                    .roles(profile.getRoles())
                    .registrationDate(profile.getRegistrationDate())
                    .build());
        }
        return list;
    }
}
