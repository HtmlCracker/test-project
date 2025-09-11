package org.example.api.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.api.dto.kafka.AccountDeletedDto;
import org.example.api.exceptions.BadRequestException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class ConsumerService {
    ProfileService profileService;

    @KafkaListener(topics = "security-service-account-delete-v1-group", groupId = "group_1")
    public void consumeAccountDeleteMessage(String message) {
        TypeReference<AccountDeletedDto> typeRef = new TypeReference<>() {};
        AccountDeletedDto dto = deserializeMessage(message, typeRef);

        log.info("Received account deletion message: accountId={}, time={}",
                dto.getAccountId(), dto.getTime());

        profileService.delProfile(dto.getAccountId());

        log.info("Account deleted: accountId={}, time={}",
                dto.getAccountId(), dto.getTime());
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
