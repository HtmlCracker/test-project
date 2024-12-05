package org.example.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.request.SendMessageRequestDto;
import org.example.api.exceptions.BadRequestException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class ConsumerService {
    EmailSenderService emailSenderService;

    @KafkaListener(topics = "email-service-v1-group", groupId = "group_id")
    public void consumeMessage(String message) {
        ArrayList<SendMessageRequestDto> dtos = deserializeMessage(message);
        emailSenderService.sendMessages(dtos);
    }

    private ArrayList<SendMessageRequestDto> deserializeMessage(String message) {
        ObjectMapper mapper = new ObjectMapper();

        TypeReference<ArrayList<SendMessageRequestDto>> typeRef = new TypeReference<>() {};

        try {
            return mapper.readValue(message, typeRef);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Request dto is not valid");
        }
    }
}
