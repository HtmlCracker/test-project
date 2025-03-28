package org.example.api.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.api.dto.request.DynamicEmailNotificationDto;
import org.example.api.exceptions.BadRequestException;
import org.example.api.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateProcessingException;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class EmailSenderService {
    @Autowired
    JavaMailSender mailSender;
    @Autowired
    TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    final String EMAIL_USERNAME;

    @Async
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000)
    )
    public void sendSimpleMessage(DynamicEmailNotificationDto dto,
                                  String htmlTemplateName,
                                  String subject) throws MessagingException {
        if (!isTemplateExists(htmlTemplateName)) {
            throw new NotFoundException("Template is not found.");
        }

        Context context = new Context();
        String htmlContent;

        try {
            context.setVariables(dto.getVariables());
            htmlContent = templateEngine.process(htmlTemplateName, context);
        } catch (TemplateProcessingException e) {
            throw new BadRequestException("Error parsing template.");
        } catch (Exception e) {
            throw new BadRequestException("Unknown error.");
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(dto.getToMail());
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.setFrom(EMAIL_USERNAME);

        try {
            mailSender.send(mimeMessage);
        } catch (MailSendException e) {
            throw e;
        }
    }

    @Recover
    public void recoverSendSimpleMessage(Exception e,
                                         DynamicEmailNotificationDto dto,
                                         String htmlTemplateName,
                                         String subject) {
        log.error("Can't send email: {}; variables: {}", dto.getToMail(), dto.getVariables().toString());
    }

    private boolean isTemplateExists(String templateName) {
        String templatePath = "templates/" + templateName + ".html";
        Resource resource = new ClassPathResource(templatePath);
        return resource.exists();
    }
}
