package org.example.api.services;

import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.api.entities.EmailQueneEntity;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;


@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class MailQueueHandlerService {
    EmailQueneService emailQueneService;
    EmailSenderService emailSenderService;
    ThreadPoolTaskExecutor asyncTaskExecutor;

    @Scheduled(fixedDelay = 3000)
    public void checkQueueAndResumeConsumer() throws InterruptedException {
        int queueSize = asyncTaskExecutor.getThreadPoolExecutor().getQueue().size();
        int THRESHOLD = 300;

        int freeSize = THRESHOLD-queueSize;

        if (freeSize > 0) {
            ArrayList<EmailQueneEntity> entities = emailQueneService.findTopNRecords(freeSize);
            log.info("Selected {} entities for processing.", entities.size());

            for (EmailQueneEntity entity : entities) {
                try {
                    sendMessagesAsync(entity);
                } catch (TaskRejectedException | MessagingException e) {
                    System.out.println("Quene is full...");
                    log.warn("Queue is full. Task rejected for entity ID: {}", entity.getId());
                }
            }
        } else {
            log.warn("Queue is full. Current queue size: {}, threshold: {}", queueSize, THRESHOLD);
        }
    }

    private void sendMessagesAsync(EmailQueneEntity entity) throws InterruptedException, MessagingException {
        emailSenderService.sendMessage(entity);
    }
}
