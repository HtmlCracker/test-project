package org.example.api.services;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.example.api.dto.request.SendMessageRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailSenderService {
    JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String EMAIL_USERNAME;

    public EmailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendMessages(ArrayList<SendMessageRequestDto> dtos) {
        for(SendMessageRequestDto dto : dtos)
            sendMessage(dto);
    }

    public void sendMessage(SendMessageRequestDto dto) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(dto.getToMail());
        mailMessage.setSubject(dto.getSubject());
        mailMessage.setText(dto.getText());
        mailMessage.setFrom(EMAIL_USERNAME);

        mailSender.send(mailMessage);
    }
}
