package com.example.securityservice.controller;

import com.example.securityservice.SecurityServiceApplication;
import com.example.securityservice.dto.UserRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@Testcontainers
@SpringBootTest(classes = SecurityServiceApplication.class)
@AutoConfigureMockMvc
public class AuthControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:alpine"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("JWT_SECRET", () -> "7e328c987520392653904d8eba9ef549f045e2cc86ee810a70a03204860585de1b7eed7d628945c1cc2a5aaa21caa01b2f707092b6d40d5955f11ebebdf1dd01598ab38d95e0f1efecfae1751a418d9a023d09aab68622e5673a4c8af3d78caa99c5a43433980a63f1a48751ce189edcc2509a1faa0d6e924903104498b1046c");
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());
        registry.add("spring.jpa.generate-ddl", () -> true);

        registry.add("spring.cloud.consul.enabled", () -> "false");
        registry.add("spring.cloud.discovery.enabled", () -> "false");
    }

    @Test
    void save_withEverythingOk() throws Exception {
        UserRequestDto userRequestDto = new UserRequestDto("test@mail.com", "Passw32#PORNHUB6969XWFor");

        var result = sendRegisterRequest(userRequestDto)
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("User has been registered"));
    }

    @Test
    void save_withInvalidEmail1() throws Exception {
        UserRequestDto userRequestDto = new UserRequestDto("test@mailcom", "Passw32#PORNHUB6969XWFor");

        var result = sendRegisterRequest(userRequestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Email should be valid"));
    }

    @Test
    void save_withInvalidEmail2() throws Exception {
        UserRequestDto userRequestDto = new UserRequestDto("test@mail.", "Passw32PO#RNHUB6969XWFor");

        var result = sendRegisterRequest(userRequestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Email should be valid"));
    }

    @Test
    void save_withInvalidEmail3() throws Exception {
        UserRequestDto userRequestDto = new UserRequestDto("test@.com", "Passw32PORNHUB#6969XWFor");

        var result = sendRegisterRequest(userRequestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Email should be valid"));
    }

    @Test
    void save_withInvalidEmail4() throws Exception {
        UserRequestDto userRequestDto = new UserRequestDto("testmail.com", "Passw32PORNHUB#6969XWFor");

        var result = sendRegisterRequest(userRequestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Email should be valid"));
    }

    @Test
    void save_withInvalidEmail5() throws Exception {
        UserRequestDto userRequestDto = new UserRequestDto("@mail.com", "Passw32POR#NHUB6969XWFor");

        var result = sendRegisterRequest(userRequestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Email should be valid"));
    }

    @Test
    void save_withBlankEmail() throws Exception {
        UserRequestDto userRequestDto = new UserRequestDto("", "Passw3#2PORNHUB6969XWFor");

        var result = sendRegisterRequest(userRequestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Email should be valid"));
    }

    @Test
    void save_withBlankPassword() throws Exception {
        UserRequestDto userRequestDto = new UserRequestDto("test@mail.com", "");

        var result = sendRegisterRequest(userRequestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Password shouldn't be empty"));
    }

    @Test
    void save_withIncorrectPassword() throws Exception {
        UserRequestDto userRequestDto = new UserRequestDto("test@mail.com", "password");

        var result = sendRegisterRequest(userRequestDto)
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("Password is not valid"));
    }

    @Test
    void save_withExistingEmail() throws Exception {
        UserRequestDto userRequestDto = new UserRequestDto("test@mail.com", "Passw3#2PORNHUB6969XWFor");

        sendRegisterRequest(userRequestDto)
                .andExpect(status().is2xxSuccessful());

        var result = sendRegisterRequest(userRequestDto)
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        assertThat(result.getResponse().getContentAsString()
                .equals("This email has already registered"));
    }

    ResultActions sendRegisterRequest(UserRequestDto userRequestDto) throws Exception {
        String requestJson = objectMapper.writeValueAsString(userRequestDto);
        return mockMvc.perform(post("http://localhost:8765/auth/public/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson));
    }
}
