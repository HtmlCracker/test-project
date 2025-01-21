package com.example.securityservice.service;

import com.example.securityservice.dto.SendMessageRequestDto;
import com.example.securityservice.exception.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProducerService {
    String TOPIK = "email-service-v1-group";
    KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessagesOrThrowException(ArrayList<SendMessageRequestDto> dtos) {
        String message = serializeIntoJsonOrThrowException(dtos);
        kafkaTemplate.send(TOPIK, message);
    }

    private String serializeIntoJsonOrThrowException(ArrayList<SendMessageRequestDto> dtos) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(dtos);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Request dto is not valid");
        }
    }
}
