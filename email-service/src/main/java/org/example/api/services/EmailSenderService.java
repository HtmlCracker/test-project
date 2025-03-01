package org.example.api.services;

import org.example.api.exceptions.NotFoundException;
import org.springframework.core.io.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.api.entities.EmailQueneEntity;
import org.example.api.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateProcessingException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class EmailSenderService {
    @Autowired
    JavaMailSender mailSender;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    EmailQueneService emailQueneService;

    @Value("${spring.mail.username}")
    String EMAIL_USERNAME;

    @Async
    public void sendMessage(EmailQueneEntity entity) throws InterruptedException, MessagingException {
        if (!isTemplateExists(entity.getHtmlTemplateName())) {
            throw new NotFoundException("Template is not found.");
        }

        Context context = new Context();
        String htmlContent;

        try {
            context.setVariables(entity.getVariables());
            htmlContent = templateEngine.process(entity.getHtmlTemplateName(), context);
        } catch (TemplateProcessingException e) {
            throw new BadRequestException("Error parsing template.");
        } catch (Exception e) {
            throw new BadRequestException("Unknown error.");
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(entity.getSendTo());
        helper.setSubject(entity.getSubject());
        helper.setText(htmlContent, true);
        helper.setFrom(EMAIL_USERNAME);

        try {
            mailSender.send(mimeMessage);
        } catch (MailSendException e) {
            Thread.sleep(3000);
            return;
        }

        emailQueneService.delete(entity);
    }

    private boolean isTemplateExists(String templateName) {
        String templatePath = "templates/" + templateName + ".html";
        Resource resource = new ClassPathResource(templatePath);
        return resource.exists();
    }
}
