package com.example.chatservice.kafka;

import com.example.chatservice.dto.EditNotification;
import com.example.chatservice.dto.Message;
import com.example.chatservice.dto.ParticipantNotification;
import com.example.chatservice.dto.ReadReceipt;
import com.example.chatservice.entity.ChatMessage;
import com.example.chatservice.repository.ChatMemberRepository;
import com.example.chatservice.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<UUID, Message> messageKafkaTemplate;
    private final KafkaTemplate<UUID, ReadReceipt> readReceiptKafkaTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatMemberRepository chatMemberRepository;

    public void sendMessage(Message message) {
        ChatMessage chatMessage = chatMessageService.saveMessage(message);
        message.setMessageId(chatMessage.getId());
        // Получаем всех участников чата (кроме отправителя)
        List<UUID> participantIds = chatMemberRepository.findParticipantIdsByChatId(message.getChatId())
                .stream()
                .filter(id -> !id.equals(message.getSenderId()))
                .collect(Collectors.toList());

        for (UUID participantId : participantIds) {
            messageKafkaTemplate.send("direct_messages", participantId, message);
        }
    }

    public void sendReadReceipt(ReadReceipt receipt) {
        // Отправляем уведомление только отправителю, чтобы он знал, что его сообщение прочитано
        // Получаем отправителя сообщения из БД
        UUID senderId = chatMessageService.findById(receipt.getMessageId()).getSenderId();

        // Отправляем уведомление отправителю
        readReceiptKafkaTemplate.send("read_receipts", senderId, receipt);
    }

    public void sendEditNotification(EditNotification notification) {
        List<UUID> participantIds = chatMemberRepository.findParticipantIdsByChatId(notification.getChatId())
                .stream()
                .filter(id -> !id.equals(notification.getSenderId()))
                .collect(Collectors.toList());

        for (UUID participantId : participantIds) {
            messageKafkaTemplate.send("direct_messages", participantId, notification.toMessage());
        }
    }

    public void sendParticipantNotification(ParticipantNotification notification) {
        List<UUID> participantIds = chatMemberRepository.findParticipantIdsByChatId(notification.getChatId())
                .stream()
                .filter(id -> !id.equals(notification.getAdminId()))
                .collect(Collectors.toList());

        for (UUID participantId : participantIds) {
            messageKafkaTemplate.send("direct_messages", participantId, notification.toMessage());
        }
    }


}