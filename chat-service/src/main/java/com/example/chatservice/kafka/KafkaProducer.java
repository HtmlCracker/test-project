package com.example.chatservice.kafka;

import com.example.chatservice.dto.EditNotification;
import com.example.chatservice.dto.Message;
import com.example.chatservice.dto.ParticipantNotification;
import com.example.chatservice.dto.ReadReceipt;
import com.example.chatservice.entity.ChatMessage;
import com.example.chatservice.repository.ChatMemberRepository;
import com.example.chatservice.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaSender<UUID, Message> kafkaSender;
    private final KafkaSender<UUID, ReadReceipt> readReceiptKafkaSender;
    private final ChatMessageService chatMessageService;
    private final ChatMemberRepository chatMemberRepository;

    public void sendMessage(Message message) {
        ChatMessage chatMessage = chatMessageService.saveMessage(message);
        message.setMessageId(chatMessage.getId());
        // Получаем всех участников чата (кроме отправителя)
        List<UUID> participantIds = chatMemberRepository.findParticipantIdsByChatId(message.getChatId())
                .stream()
                .filter(id -> !id.equals(message.getSenderId()))
                .toList();
        // Создаем поток сообщений для отправки
        Flux<SenderRecord<UUID, Message, UUID>> records = Flux.fromIterable(participantIds)
                .map(participantId ->
                        SenderRecord.create(
                                // topic, key (chatId для партиционирования), value (message)
                                new ProducerRecord<>("direct_messages", message.getChatId(), toMessageDto(chatMessage)),
                                participantId  // Correlation metadata (recipientId)
                        )
                );
        // Отправляем сообщения асинхронно
        kafkaSender.send(records)
                .doOnError(e ->
                        System.err.println("Error sending message to Kafka: " + e.getMessage())
                )
                .doOnComplete(() ->
                        System.out.println("Successfully sent " + participantIds.size() + " messages")
                )
                .retryWhen(
                        Retry.backoff(3, Duration.ofMillis(100))
                                .doBeforeRetry(retrySignal -> {
                                    System.out.println("Retry #" + retrySignal.totalRetries() +
                                            " for message send. Error: " + retrySignal.failure().getMessage());
                                })
                )
                .subscribe();
    }

    public void sendReadReceipt(ReadReceipt receipt) {
        // Отправляем уведомление только отправителю, чтобы он знал, что его сообщение прочитано
        // Получаем отправителя сообщения из БД
        UUID senderId = chatMessageService.findById(receipt.getMessageId()).getSenderId();

        SenderRecord<UUID, ReadReceipt, UUID> record = SenderRecord.create(
                new ProducerRecord<>("read_receipts", senderId, receipt),
                senderId
        );

        readReceiptKafkaSender.send(Mono.just(record))
                .doOnError(e ->
                        System.err.println("Error sending read receipt: " + e.getMessage())
                )
                .subscribe();
    }

    public void sendEditNotification(EditNotification notification) {
        List<UUID> participantIds = chatMemberRepository.findParticipantIdsByChatId(notification.getChatId())
                .stream()
                .filter(id -> !id.equals(notification.getSenderId()))
                .collect(Collectors.toList());

        // Создаем поток сообщений для отправки
        Flux<SenderRecord<UUID, Message, UUID>> records = Flux.fromIterable(participantIds)
                .map(participantId ->
                        SenderRecord.create(
                                new ProducerRecord<>("direct_messages", notification.getChatId(), notification.toMessage()),
                                participantId
                        )
                );

        kafkaSender.send(records)
                .doOnError(e ->
                        System.err.println("Error sending edit notification: " + e.getMessage())
                )
                .doOnComplete(() ->
                        System.out.println("Successfully sent " + participantIds.size() + " edit notifications")
                )
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .subscribe();
    }

    public void sendParticipantNotification(ParticipantNotification notification) {
        List<UUID> participantIds = chatMemberRepository.findParticipantIdsByChatId(notification.getChatId())
                .stream()
                .filter(id -> !id.equals(notification.getAdminId()))
                .collect(Collectors.toList());

        Flux<SenderRecord<UUID, Message, UUID>> records = Flux.fromIterable(participantIds)
                .map(participantId ->
                        SenderRecord.create(
                                new ProducerRecord<>("direct_messages", notification.getChatId(), notification.toMessage()),
                                participantId
                        )
                );

        kafkaSender.send(records)
                .doOnError(e ->
                        System.err.println("Error sending participant notification: " + e.getMessage())
                )
                .doOnComplete(() ->
                        System.out.println("Successfully sent " + participantIds.size() + " participant notifications")
                )
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .subscribe();
    }

    private Message toMessageDto(ChatMessage chatMessage) {
        Message message = new Message();
        message.setMessageId(chatMessage.getId());
        message.setSenderId(chatMessage.getSenderId());
        message.setChatId(chatMessage.getChatId());
        message.setText(chatMessage.getText());
        message.setTimestamp(chatMessage.getTimestamp());
        message.setEdited(chatMessage.isEdited());
        message.setStatus(chatMessage.getStatus());
        return message;
    }
}