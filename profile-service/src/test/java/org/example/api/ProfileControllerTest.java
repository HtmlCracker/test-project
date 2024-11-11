package org.example.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.api.controllers.ProfileController;
import org.example.api.dto.request.ProfileRegistrationRequestDto;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


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
        registerProfile_WithSomeEmailAndName_ShouldThrowOk(email, "test");
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
        registerProfile_WithSomeEmailAndName_ShouldThrowOk("test@test.ru", name);
    }


    @Test
    void updateProfile_WithBadProfileId_ShouldThrowNotFoundException() throws Exception {
        ProfileRegistrationRequestDto requestDto = makeProfileRegistrationRequestDto("test@test.ru", "test", "test", "", "");

        String id = "fsdfsdfdsfsdf";

        RequestBuilder request = mockMvcBuilder.post("/api/public/accounts/update/"+id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto));

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isNotFound()).andReturn();
    }

    void registerProfile_WithSomeEmailAndName_ShouldThrowBadRequest(String email, String name) throws Exception {
        ProfileRegistrationRequestDto requestDto = makeProfileRegistrationRequestDto(email, name, "test", "", "");

        RequestBuilder request = mockMvcBuilder.post("/api/public/accounts/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto));

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isBadRequest()).andReturn();

    }

    void registerProfile_WithSomeEmailAndName_ShouldThrowOk(String email, String name) throws Exception {
        ProfileRegistrationRequestDto requestDto = makeProfileRegistrationRequestDto(email, name, "test", "", "");

        RequestBuilder request = mockMvcBuilder.post("/api/public/accounts/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto));

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk()).andReturn();

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

}



