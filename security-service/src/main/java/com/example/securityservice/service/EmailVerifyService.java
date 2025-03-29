package com.example.securityservice.service;

import com.example.securityservice.dto.SendToMailRequestDto;
import com.example.securityservice.entity.EmailVerifyEntity;
import com.example.securityservice.exception.BadRequestException;
import com.example.securityservice.repository.EmailVerifyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class EmailVerifyService {
    EmailVerifyRepository emailVerifyRepository;

    ProducerService producerService;
    UserService userService;

    public void sendForgotPassword(String email, UUID token) {
        if (email.isEmpty()) {
            throw new BadRequestException("Email shouldn't be empty");
        }

        HashMap<String, Object> variables = new HashMap<>();
        variables.put("token", token.toString());

        SendToMailRequestDto sendToMailRequestDto =
                SendToMailRequestDto.builder()
                        .toMail(email)
                        .variables(variables)
                        .build();

        producerService.sendMessageChangePasswordOrThrowException(sendToMailRequestDto);
    }

    public void sendVerifyToken(String email) {
        if (email.isEmpty()) {
            throw new BadRequestException("Email shouldn't be empty");
        }

        UUID token = genVerifyToken();

        EmailVerifyEntity entity = EmailVerifyEntity.builder()
                .email(email)
                .token(token)
                .build();

        saveVerifyTokenEntity(entity);

        SendToMailRequestDto dto = genSendMessageDto(email, token);
        producerService.sendMessageConfirmMailOrThrowException(dto);
    }

    private UUID genVerifyToken() {
        return UUID.randomUUID();
    }

    private void saveVerifyTokenEntity(EmailVerifyEntity entity) {
        emailVerifyRepository.saveAndFlush(entity);
    }

    private SendToMailRequestDto genSendMessageDto(String email, UUID token) {
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("token", token.toString());

        return SendToMailRequestDto.builder()
                .toMail(email)
                .variables(variables)
                .build();
    }

    public void activateEmail(String token) {
        UUID UUIDToken = UUID.fromString(token);

        EmailVerifyEntity entity = emailVerifyRepository.findByToken(UUIDToken)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Token %s is not exists", token)
                ));

        String confirmedEmail = entity.getEmail();
        emailVerifyRepository.delete(entity);

        userService.updateEnabled(confirmedEmail, true);
    }
}
