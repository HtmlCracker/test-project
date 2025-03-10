package com.example.securityservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.securityservice.dto.RequestDto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.generate-ddl", () -> true);
    }

    @Test
    void save_withEverythingOk() throws Exception {
        RequestDto requestDto = new RequestDto("test@mail.com", "password");

        var result = sendRegisterRequest(requestDto)
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("User has been registered"));
    }

    @Test
    void save_withInvalidEmail1() throws Exception {
        RequestDto requestDto = new RequestDto("test@mailcom", "password");

        var result = sendRegisterRequest(requestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Email should be valid"));
    }

    @Test
    void save_withInvalidEmail2() throws Exception {
        RequestDto requestDto = new RequestDto("test@mail.", "password");

        var result = sendRegisterRequest(requestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Email should be valid"));
    }

    @Test
    void save_withInvalidEmail3() throws Exception {
        RequestDto requestDto = new RequestDto("test@.com", "password");

        var result = sendRegisterRequest(requestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Email should be valid"));
    }

    @Test
    void save_withInvalidEmail4() throws Exception {
        RequestDto requestDto = new RequestDto("testmail.com", "password");

        var result = sendRegisterRequest(requestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Email should be valid"));
    }

    @Test
    void save_withInvalidEmail5() throws Exception {
        RequestDto requestDto = new RequestDto("@mail.com", "password");

        var result = sendRegisterRequest(requestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Email should be valid"));
    }

    @Test
    void save_withBlankEmail() throws Exception {
        RequestDto requestDto = new RequestDto("", "password");

        var result = sendRegisterRequest(requestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Email should be valid"));
    }

    @Test
    void save_withBlankPassword() throws Exception {
        RequestDto requestDto = new RequestDto("test@mail.com", "");

        var result = sendRegisterRequest(requestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Password shouldn't be empty"));
    }

    @Test
    void save_withExistingEmail() throws Exception {
        RequestDto requestDto = new RequestDto("test@mail.com", "password");

        sendRegisterRequest(requestDto)
                .andExpect(status().is2xxSuccessful());

        var result = sendRegisterRequest(requestDto)
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("This email has already registered"));
    }

    ResultActions sendRegisterRequest(RequestDto requestDto) throws Exception {
        String requestJson = objectMapper.writeValueAsString(requestDto);
        return mockMvc.perform(post("http://localhost:8765/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson));
    }

}
