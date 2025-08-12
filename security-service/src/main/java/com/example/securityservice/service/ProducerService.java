package com.example.securityservice.service;

import ch.qos.logback.core.rolling.SizeAndTimeBasedFileNamingAndTriggeringPolicy;
import com.example.securityservice.dto.kafka.AccountDeletedDto;
import com.example.securityservice.dto.kafka.SendToMailRequestDto;
import com.example.securityservice.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProducerService {
    static String TOPIC_DELETE_ACCOUNT = "security-service-account-delete-v1-group";
    static String TOPIC_CONFIRM_MAIL = "account-verification-emails-v1-group";
    static String TOPIC_CHANGE_PASSWORD = "password-change-emails-v1-group";

    KafkaTemplate<String, String> kafkaTemplate;

    public void sendDeletedAccountMessage(AccountDeletedDto dto) {
        String message = serializeIntoJsonOrThrowException(dto);
        kafkaTemplate.send(TOPIC_DELETE_ACCOUNT, message);
    }

    public void sendMessageConfirmMailOrThrowException(SendToMailRequestDto dto) {
        String message = serializeIntoJsonOrThrowException(dto);
        kafkaTemplate.send(TOPIC_CONFIRM_MAIL, message);
    }

    public void sendMessageChangePasswordOrThrowException(SendToMailRequestDto dto) {
        String message = serializeIntoJsonOrThrowException(dto);
        kafkaTemplate.send(TOPIC_CHANGE_PASSWORD, message);
    }

    private <T> String serializeIntoJsonOrThrowException(T dto) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Request dto is not valid");
        }
    }
}
