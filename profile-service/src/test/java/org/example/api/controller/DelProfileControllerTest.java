package org.example.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.controllers.ProfileController;
import org.example.api.dto.response.DelProfileResponseDto;
import org.example.api.entities.ProfileEntity;
import org.example.api.exceptions.NotFoundException;
import org.example.api.factories.ProfileDtoFactory;
import org.example.api.services.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProfileController.class)
public class DelProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private ProfileDtoFactory profileDtoFactory;

    @Test
    void delProfile_shouldReturnDelProfileResponseDto() throws Exception {
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

        String status = "The profile has been deleted.";

        DelProfileResponseDto response = DelProfileResponseDto.builder()
                .status(status)
                .build();

        when(profileService.getProfileByIdOrThrowException(any()))
                .thenReturn(mockEntity);
        when(profileService.delProfile(profileId))
                .thenReturn(response);

        mockMvc.perform(delete("/api/private/accounts/del/{profileId}", profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(status))
                .andReturn();
    }

    @Test
    void delProfile_shouldThrowNotFoundException() throws Exception {
        UUID profileId = UUID.randomUUID();

        when(profileService.delProfile(profileId))
                .thenThrow(new NotFoundException("Account with id " + profileId + " does not exist"));

        mockMvc.perform(delete("/api/private/accounts/del/{profileId}", profileId))
                .andExpect(status().isNotFound());
    }
}