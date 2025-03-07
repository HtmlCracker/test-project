package org.example.api.services;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.request.SendMessageRequestDto;
import org.example.api.entities.EmailQueneEntity;
import org.example.api.repositories.EmailQueneRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailQueneService {
    EmailQueneRepository emailQueneRepository;

    public void addMessagesToQueue(ArrayList<SendMessageRequestDto> dtos) {
        ArrayList<EmailQueneEntity> mailQueueEntities = dtos.stream()
                .map(this::mailQueueEntityBuilder)
                .collect(Collectors.toCollection(ArrayList::new));

        emailQueneRepository.saveAll(mailQueueEntities);
    }

    private EmailQueneEntity mailQueueEntityBuilder(SendMessageRequestDto dto) {
        return EmailQueneEntity.builder()
                .sendTo(dto.getToMail())
                .subject(dto.getSubject())
                .htmlTemplateName(dto.getHtmlTemplateName())
                .variables(dto.getVariables())
                .build();
    }

    public ArrayList<EmailQueneEntity> findTopNRecordsByPriority(int count) {
        return emailQueneRepository.findTopNRecordsByPriority(count);
    }

    public void delete(EmailQueneEntity entity) {
        emailQueneRepository.delete(entity);
    }
}
