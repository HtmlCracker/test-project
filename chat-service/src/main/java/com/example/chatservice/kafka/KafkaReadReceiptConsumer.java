package com.example.chatservice.kafka;

import com.example.chatservice.dto.ReadReceipt;
import com.example.chatservice.service.ChatMessageService;
import com.example.chatservice.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaReadReceiptConsumer {
    private final ChatMessageService chatMessageService;
    private final ChatWebSocketHandler webSocketHandler;

    @KafkaListener(topics = "read_receipts", groupId = "read-receipt-group", containerFactory = "readReceiptKafkaListenerFactory")
    public void handleReadReceipt(ConsumerRecord<UUID, ReadReceipt> record, Acknowledgment acknowledgment) {
        //честно, это выглядит как очень багованнное место
        //как только появится фронт надо тестить со всех сил
        ReadReceipt receipt = record.value();
        try {
            chatMessageService.markAsRead(receipt.getMessageId(), receipt.getRecipientId());
            UUID userIdToNotify = record.key();
            webSocketHandler.sendReadReceipt(userIdToNotify, receipt);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            e.printStackTrace();
            acknowledgment.acknowledge();
        }
    }
}
