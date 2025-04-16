package org.example.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.controllers.ProfileController;
import org.example.api.dto.request.ProfileRegistrationRequestDto;
import org.example.api.dto.request.ProfileUpdateRequestDto;
import org.example.api.dto.response.DelProfileResponseDto;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProfileController.class)
public class ProfileControllerTest {
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
    void registerProfile_WithNullEmail_ShouldThrowBadRequest() throws Exception {
        String email = null;
        registerProfile_WithSomeEmailAndName_ShouldThrowBadRequest(email, "test");
    }

    @Test
    void registerProfile_WithEmailWithoutAtSymbol_ShouldThrowBadRequest() throws Exception {
        String email = "testtest.ru";
        registerProfile_WithSomeEmailAndName_ShouldThrowBadRequest(email, "test");
    }

    @Test
    void registerProfile_WithEmailMissingDomain_ShouldThrowBadRequest() throws Exception {
        String email = "test@test.";
        registerProfile_WithSomeEmailAndName_ShouldThrowBadRequest(email, "test");
    }

    @Test
    void registerProfile_WithEmailNoDot_ShouldThrowBadRequest() throws Exception {
        String email = "test@testru";
        registerProfile_WithSomeEmailAndName_ShouldThrowBadRequest(email, "test");
    }

    @Test
    void registerProfile_WithEmailOk_ShouldThrowOk() throws Exception {
        String email = "test@test.ru";
        registerProfile_WithSomeEmailAndName_ShouldBeOk(email, "test");
    }

    @Test
    void registerProfile_WithVoidName_ShouldThrowBadRequest() throws Exception {
        String name = "";
        registerProfile_WithSomeEmailAndName_ShouldThrowBadRequest("test@test.ru", name);
    }

    @Test
    void registerProfile_WithNullName_ShouldThrowBadRequest() throws Exception {
        String name = null;
        registerProfile_WithSomeEmailAndName_ShouldThrowBadRequest("test@test.ru", name);
    }

    @Test
    void registerProfile_WithOkName_ShouldThrowOk() throws Exception {
        String name = "test";
        registerProfile_WithSomeEmailAndName_ShouldBeOk("test@test.ru", name);
    }

    void registerProfile_WithSomeEmailAndName_ShouldThrowBadRequest(String email, String name) throws Exception {
        ProfileRegistrationRequestDto requestDto = makeProfileRegistrationRequestDto(email, name, "test", "", "");

        RequestBuilder request = mockMvcBuilder.post("/api/public/accounts/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto));

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isBadRequest()).andReturn();
    }

    void registerProfile_WithSomeEmailAndName_ShouldBeOk(String email, String name) throws Exception {
        ProfileRegistrationRequestDto requestDto = makeProfileRegistrationRequestDto(email, name, "test", "", "");

        RequestBuilder request = mockMvcBuilder.post("/api/public/accounts/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto));

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk()).andReturn();


    }

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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/private/accounts/update/{profileId}", profileId)
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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/private/accounts/update/{profileId}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound()).andReturn();
    }

    @Test
    void getProfile_shouldReturnProfile() throws Exception {
        UUID profileId = UUID.randomUUID();
        ProfileEntity mockEntity = buildTestProfile(profileId);
        ProfileResponseDto responseDto = buildTestProfileDto(mockEntity);

        when(profileService.getProfileByIdOrThrowException(profileId)).thenReturn(mockEntity);
        when(profileDtoFactory.makeProfileDto(mockEntity)).thenReturn(responseDto);

        mockMvc.perform(get("/api/private/accounts/get/{profileId}", profileId))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(profileId.toString()),
                        jsonPath("$.email").value("test@test.test"),
                        jsonPath("$.name").value("test"),
                        jsonPath("$.surname").value("test"),
                        jsonPath("$.description").value(""),
                        jsonPath("$.roles").value(empty())
                );
    }

    @Test
    void getProfile_shouldThrowNotFoundException() throws Exception {
        UUID profileId = UUID.randomUUID();
        String errorMessage = "Account with id " + profileId + " does not exist";

        when(profileService.getProfileByIdOrThrowException(profileId))
                .thenThrow(new NotFoundException(errorMessage));

        mockMvc.perform(get("/api/private/accounts/get/{profileId}", profileId))
                .andExpectAll(
                        status().isNotFound()
                );
    }

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
    void delProfile_shouldThrowBadRequestException() throws Exception {
        UUID profileId = UUID.randomUUID();

        when(profileService.delProfile(profileId))
                .thenThrow(new NotFoundException("Account with id " + profileId + " does not exist"));

        mockMvc.perform(delete("/api/private/accounts/del/{profileId}", profileId))
                .andExpect(status().isNotFound());
    }

    private ProfileRegistrationRequestDto makeProfileRegistrationRequestDto (
            String email,
            String name,
            String surname,
            String description,
            String roles
    ) {
        return ProfileRegistrationRequestDto.builder()
                .email(email)
                .name(name)
                .surname(surname)
                .description(description)
                .roles(roles)
                .build();
    }

    private ProfileEntity buildTestProfile(UUID id) {
        return ProfileEntity.builder()
                .id(id)
                .email("test@test.test")
                .name("test")
                .surname("test")
                .description("")
                .roles(Collections.emptyList())
                .registrationDate(Instant.now())
                .build();
    }

    private ProfileResponseDto buildTestProfileDto(ProfileEntity entity) {
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
    private ResultActions performUpdateRequest(UUID profileId, ProfileUpdateRequestDto requestDto) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post("/api/private/accounts/update/{profileId}", profileId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));
    }

}
