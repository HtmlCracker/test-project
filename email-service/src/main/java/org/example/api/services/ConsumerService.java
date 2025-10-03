package org.example.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.example.api.dto.kafka.JoinToProjectRequestDto;
import org.example.api.dto.request.DynamicEmailNotificationDto;
import org.example.api.exceptions.BadRequestException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class ConsumerService {
    EmailSenderService emailSenderService;

    @KafkaListener(topics = "account-verification-emails-v1-group", groupId = "group_1")
    public void consumeAccountVerifyMessage(String message) throws Exception {
        TypeReference<DynamicEmailNotificationDto> typeRef = new TypeReference<>() {};
        DynamicEmailNotificationDto dto = deserializeMessage(message, typeRef);

        emailSenderService.sendSimpleMessage(dto, "mailConfirm", "account verify");
    }

    @KafkaListener(topics = "password-change-emails-v1-group", groupId = "group_2")
    public void consumeChangePasswordMessage(String message) throws Exception {
        TypeReference<DynamicEmailNotificationDto> typeRef = new TypeReference<>() {};
        DynamicEmailNotificationDto dto = deserializeMessage(message, typeRef);

        emailSenderService.sendSimpleMessage(dto, "changePasswordMessage", "Change password");
    }

    @KafkaListener(topics = "project-profile-service-join-to-project-request-v1-group",
            groupId = "group_1")
    public void consumeJoinToProjectRequest(String message) throws MessagingException {
        TypeReference<JoinToProjectRequestDto> typeRef = new TypeReference<>() {};
        JoinToProjectRequestDto joinToProjectRequestDto = deserializeMessage(message, typeRef);

        HashMap<String, String> variables = new HashMap<>();
        variables.put("userId", joinToProjectRequestDto.getUserId().toString());
        variables.put("message", joinToProjectRequestDto.getMessage());

        DynamicEmailNotificationDto dynamicEmailNotificationDto =
                DynamicEmailNotificationDto.builder()
                        .toMail()
                        .variables(variables)
                        .build();

        emailSenderService.sendSimpleMessage(
                dynamicEmailNotificationDto,
                "Nontify",
                "New join request");
    }

    private <T> T deserializeMessage(String message, TypeReference<T> typeRef) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(message, typeRef);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Request dto is not valid");
        }
    }
}
