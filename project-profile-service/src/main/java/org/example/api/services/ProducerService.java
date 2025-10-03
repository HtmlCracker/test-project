package org.example.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.kafka.JoinToProjectKafkaDto;
import org.example.api.exceptions.BadRequestException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProducerService {
    public static final String JOIN_TO_PROJECT_REQUEST =
            "project-profile-service-join-to-project-request-v1-group";

    KafkaTemplate<String, String> kafkaTemplate;

    public void sendJoinToProjectMessage(JoinToProjectKafkaDto dto) {
        String message = serializeIntoJsonOrThrowException(dto);
        kafkaTemplate.send(JOIN_TO_PROJECT_REQUEST, message);
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
