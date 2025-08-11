package org.example.api.controller;

import org.example.api.controllers.ProfileController;
import org.example.api.dto.request.ProfileRegistrationRequestDto;
import org.example.api.dto.request.ProfileUpdateRequestDto;
import org.example.api.dto.response.DelProfileResponseDto;
import org.example.api.dto.response.ProfileResponseDto;
import org.example.api.entities.ProfileEntity;
import org.example.api.factories.ProfileDtoFactory;
import org.example.api.services.ProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfileControllerTest {
    @Mock
    private ProfileService profileService;

    @Mock
    private ProfileDtoFactory profileDtoFactory;

    @InjectMocks
    private ProfileController profileController;

    @Test
    void registrationProfile_ValidRequest_ReturnsProfileResponseDto() {
        ProfileRegistrationRequestDto requestDto = ProfileRegistrationRequestDto.builder()
                .email("test@test.test")
                .name("test")
                .surname("test")
                .roles("test")
                .build();

        ProfileEntity mockProfile = new ProfileEntity();
        ProfileResponseDto expectedResponse = ProfileResponseDto.builder()
                .id(UUID.randomUUID())
                .email("test@test.test")
                .name("test")
                .surname("test")
                .roles(List.of("test"))
                .registrationDate(Instant.now())
                .build();

        when(profileService.registerProfile(requestDto)).thenReturn(mockProfile);
        when(profileDtoFactory.makeProfileDto(mockProfile)).thenReturn(expectedResponse);

        ProfileResponseDto response = profileController.registrationProfile(requestDto);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(profileService).registerProfile(requestDto);
        verify(profileDtoFactory).makeProfileDto(mockProfile);
    }

    @Test
    void updateProfile_ValidRequest_ReturnsUpdatedProfile() {
        UUID profileId = UUID.randomUUID();
        ProfileUpdateRequestDto requestDto = ProfileUpdateRequestDto.builder()
                .name("Updated Name")
                .surname("Updated Surname")
                .build();

        ProfileEntity updatedProfile = new ProfileEntity();
        updatedProfile.setId(profileId);
        updatedProfile.setName("Updated Name");
        updatedProfile.setSurname("Updated Surname");

        ProfileResponseDto expectedResponse = ProfileResponseDto.builder()
                .id(profileId)
                .email("test@test.test")
                .name("Updated Name")
                .roles(List.of("test"))
                .registrationDate(Instant.now())
                .surname("Updated Surname")
                .build();

        when(profileService.updateProfile(profileId, requestDto)).thenReturn(updatedProfile);
        when(profileDtoFactory.makeProfileDto(updatedProfile)).thenReturn(expectedResponse);

        ProfileResponseDto response = profileController.updateProfile(profileId, requestDto);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(profileService).updateProfile(profileId, requestDto);
        verify(profileDtoFactory).makeProfileDto(updatedProfile);
    }

    @Test
    void getProfileDto_ExistingProfile_ReturnsProfile() {
        UUID profileId = UUID.randomUUID();
        ProfileEntity mockProfile = new ProfileEntity();
        mockProfile.setId(profileId);
        mockProfile.setName("Test User");

        ProfileResponseDto expectedResponse = ProfileResponseDto.builder()
                .id(profileId)
                .email("test@test.test")
                .roles(List.of("test"))
                .registrationDate(Instant.now())
                .name("Test User")
                .build();

        when(profileService.getProfileByIdOrThrowException(profileId)).thenReturn(mockProfile);
        when(profileDtoFactory.makeProfileDto(mockProfile)).thenReturn(expectedResponse);

        ProfileResponseDto response = profileController.getProfileDto(profileId);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(profileService).getProfileByIdOrThrowException(profileId);
        verify(profileDtoFactory).makeProfileDto(mockProfile);
    }

    @Test
    void delProfile_ExistingProfile_ReturnsSuccessResponse() {
        UUID profileId = UUID.randomUUID();
        DelProfileResponseDto expectedResponse = DelProfileResponseDto.builder()
                .status("Profile deleted")
                .build();

        when(profileService.delProfile(profileId)).thenReturn(expectedResponse);

        DelProfileResponseDto response = profileController.delProfile(profileId);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(profileService).delProfile(profileId);
    }
}
