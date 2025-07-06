package org.example.api.services;

import org.example.api.dto.request.ProfileRegistrationRequestDto;
import org.example.api.dto.request.ProfileUpdateRequestDto;
import org.example.api.dto.response.DelProfileResponseDto;
import org.example.api.entities.ProfileEntity;
import org.example.api.exceptions.BadRequestException;
import org.example.api.exceptions.NotFoundException;
import org.example.api.repositories.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {
    @Mock
    ProfileRepository profileRepository;

    @InjectMocks
    ProfileService profileService;

    @Test
    void registerProfile_shouldThrowBadRequestException() {
        UUID id = UUID.randomUUID();

        ProfileRegistrationRequestDto dto = ProfileRegistrationRequestDto.builder()
                .email("test@test.test")
                .name("test")
                .roles("test")
                .build();

        ProfileEntity profile = ProfileEntity.builder().build();

        when(profileRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(profile));

        assertThrows(BadRequestException.class, () -> profileService.registerProfile(dto));
    }

    @Test
    void registerProfile_shouldReturnProfileEntity() {
        UUID id = UUID.randomUUID();

        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        ProfileRegistrationRequestDto dto = ProfileRegistrationRequestDto.builder()
                .email("test@test.test")
                .name("test")
                .roles("ROLE_USER,ROLE_ADMIN")
                .build();

        ProfileEntity profile = ProfileEntity.builder()
                .id(id)
                .email(dto.getEmail())
                .name(dto.getName())
                .roles(roles)
                .build();

        when(profileRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.empty());
        when(profileRepository.save(any(ProfileEntity.class)))
                .thenReturn(profile);

        assertEquals(profileService.registerProfile(dto), profile);
    }

    @Test
    void updateProfile_shouldThrowNotFoundException() {
        UUID id = UUID.randomUUID();
        ProfileUpdateRequestDto dto = ProfileUpdateRequestDto.builder()
                .email("test@test.test")
                .name("test")
                .roles("test")
                .build();

        when(profileRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> profileService.updateProfile(id, dto));
    }

    @Test
    void updateProfile_shouldReturnProfileEntity() {
        UUID id = UUID.randomUUID();
        List<String> roles = List.of("newTest");

        ProfileUpdateRequestDto dto = ProfileUpdateRequestDto.builder()
                .email("test@test.test")
                .name("newTest")
                .roles("newRole")
                .build();

        ProfileEntity profile = ProfileEntity.builder()
                .id(id)
                .email(dto.getEmail())
                .name("oldTest")
                .build();

        ProfileEntity updatedProfile = ProfileEntity.builder()
                .id(id)
                .email(dto.getEmail())
                .name("newTest")
                .roles(roles)
                .build();

        when(profileRepository.findById(id))
                .thenReturn(Optional.of(profile));
        when(profileRepository.save(any(ProfileEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<ProfileEntity> profileCaptor = ArgumentCaptor.forClass(ProfileEntity.class);

        ProfileEntity result = profileService.updateProfile(id, dto);

        verify(profileRepository).save(profileCaptor.capture());

        ProfileEntity savedProfile = profileCaptor.getValue();

        assertEquals(updatedProfile.getEmail(), savedProfile.getEmail());
        assertEquals(updatedProfile.getName(), savedProfile.getName());
        assertEquals(updatedProfile.getRoles(), roles);

        assertSame(savedProfile, result);
    }

    @Test
    void delProfile_shouldThrowNotFoundException() {
        UUID id = UUID.randomUUID();

        when(profileRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> profileService.delProfile(id));
    }

    @Test
    void delProfile_shouldReturnDelProfileResponseDto() {
        UUID id = UUID.randomUUID();

        ProfileEntity profile = ProfileEntity.builder().build();

        DelProfileResponseDto dto = DelProfileResponseDto.builder()
                .status("The profile has been deleted.")
                .build();

        when(profileRepository.findById(id))
                .thenReturn(Optional.of(profile));
        doNothing().when(profileRepository).delete(any(ProfileEntity.class));

        assertEquals(profileService.delProfile(id), dto);
        verify(profileRepository).delete(any(ProfileEntity.class));
    }
}
