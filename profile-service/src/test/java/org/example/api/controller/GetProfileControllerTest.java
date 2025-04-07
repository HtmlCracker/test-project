package org.example.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.controllers.ProfileController;
import org.example.api.dto.response.ProfileResponseDto;
import org.example.api.entities.ProfileEntity;
import org.example.api.exceptions.NotFoundException;
import org.example.api.factories.ProfileDtoFactory;
import org.example.api.services.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProfileController.class)
public class GetProfileControllerTest {
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
    void getProfile_shouldReturnProfile() throws Exception {
        UUID profileId = UUID.randomUUID();

        List<String> roles = Collections.emptyList();

        ProfileEntity mockEntity = ProfileEntity.builder()
                .id(profileId)
                .email("test@test.test")
                .name("test")
                .surname("test")
                .description("")
                .roles(roles)
                .registrationDate(Instant.now())
                .build();

        ProfileResponseDto response = ProfileResponseDto.builder()
                .id(mockEntity.getId())
                .email(mockEntity.getEmail())
                .name(mockEntity.getName())
                .surname(mockEntity.getSurname())
                .description(mockEntity.getDescription())
                .roles(mockEntity.getRoles())
                .registrationDate(mockEntity.getRegistrationDate())
                .build();

        when(profileService.getProfileByIdOrThrowException(any()))
                .thenReturn(mockEntity);
        when(profileDtoFactory.makeProfileDto(mockEntity))
                .thenReturn(response);

        mockMvc.perform(get("/api/private/accounts/get/{profileId}", profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profileId.toString()))
                .andExpect(jsonPath("$.email").value(mockEntity.getEmail()))
                .andExpect(jsonPath("$.name").value(mockEntity.getName()))
                .andExpect(jsonPath("$.surname").value(mockEntity.getSurname()))
                .andExpect(jsonPath("$.description").value(mockEntity.getDescription()))
                .andExpect(jsonPath("$.roles").value(roles == null ? nullValue() : empty()))
                .andReturn();

    }

    @Test
    void getProfile_shouldThrowNotFoundException() throws Exception {
        UUID profileId = UUID.randomUUID();

        when(profileService.getProfileByIdOrThrowException(any()))
                .thenThrow(new NotFoundException("Account with id " + profileId + " does not exist"));

        mockMvc.perform(get("/api/private/accounts/get/{profileId}", profileId))
                .andExpect(status().isNotFound());
    }
}
