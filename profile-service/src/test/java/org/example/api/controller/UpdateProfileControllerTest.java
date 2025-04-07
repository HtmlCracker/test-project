package org.example.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.controllers.ProfileController;
import org.example.api.dto.request.ProfileUpdateRequestDto;
import org.example.api.dto.response.ProfileResponseDto;
import org.example.api.entities.ProfileEntity;
import org.example.api.exceptions.NotFoundException;
import org.example.api.factories.ProfileDtoFactory;
import org.example.api.services.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProfileController.class)
public class UpdateProfileControllerTest {
    private MockMvcRequestBuilders mockMvcBuilder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private ProfileDtoFactory profileDtoFactory;

    @Test
    void updateProfile_shouldReturnUpdatedProfile() throws Exception {
        UUID profileId = UUID.randomUUID();
        Instant registrationDate = Instant.now();
        ProfileUpdateRequestDto requestDto = ProfileUpdateRequestDto.builder()
                .email("test@test.test")
                .name("test_updated")
                .surname("")
                .description("")
                .roles("")
                .build();

        ProfileEntity mockEntity = ProfileEntity.builder()
                .id(profileId)
                .email(requestDto.getEmail())
                .name(requestDto.getName())
                .surname(requestDto.getSurname())
                .description(requestDto.getDescription())
                .roles(Collections.singletonList(requestDto.getRoles()))
                .build();

        ProfileResponseDto expectedResponse = ProfileResponseDto.builder()
                .id(profileId)
                .email(requestDto.getEmail())
                .name(requestDto.getName())
                .surname(requestDto.getSurname())
                .description(requestDto.getDescription())
                .roles(Collections.singletonList(requestDto.getRoles()))
                .registrationDate(registrationDate)
                .build();

        when(profileService.updateProfile(eq(profileId), any(ProfileUpdateRequestDto.class)))
                .thenReturn(mockEntity);
        when(profileDtoFactory.makeProfileDto(mockEntity))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/private/accounts/update/{profileId}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profileId.toString()))
                .andExpect(jsonPath("$.email").value(requestDto.getEmail()))
                .andExpect(jsonPath("$.name").value(requestDto.getName()))
                .andExpect(jsonPath("$.surname").value(requestDto.getSurname()))
                .andExpect(jsonPath("$.description").value(requestDto.getDescription()))
                .andExpect(jsonPath("$.roles[0]").value(requestDto.getRoles()));
    }

    @Test
    void updateProfile_shouldThrowNotFoundException() throws Exception {
        UUID profileId = UUID.randomUUID();
        Instant registrationDate = Instant.now();
        ProfileUpdateRequestDto requestDto = ProfileUpdateRequestDto.builder()
                .email("test@test.test")
                .name("test_updated")
                .surname("")
                .description("")
                .roles("")
                .build();

        ProfileEntity mockEntity = ProfileEntity.builder()
                .id(profileId)
                .email(requestDto.getEmail())
                .name(requestDto.getName())
                .surname(requestDto.getSurname())
                .description(requestDto.getDescription())
                .roles(Collections.singletonList(requestDto.getRoles()))
                .build();

        ProfileResponseDto expectedResponse = ProfileResponseDto.builder()
                .id(profileId)
                .email(requestDto.getEmail())
                .name(requestDto.getName())
                .surname(requestDto.getSurname())
                .description(requestDto.getDescription())
                .roles(Collections.singletonList(requestDto.getRoles()))
                .registrationDate(registrationDate)
                .build();

        when(profileService.updateProfile(any(), any()))
                .thenThrow(new NotFoundException("Account with id " + profileId.toString() + " does not exist"));
        when(profileDtoFactory.makeProfileDto(mockEntity))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/api/private/accounts/update/{profileId}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound()).andReturn();
    }
}
