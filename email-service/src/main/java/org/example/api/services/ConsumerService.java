package org.example.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.api.dto.request.DynamicEmailNotificationDto;
import org.example.api.exceptions.BadRequestException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

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

    private <T> T deserializeMessage(String message, TypeReference<T> typeRef) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(message, typeRef);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Request dto is not valid");
        }
    }
}
