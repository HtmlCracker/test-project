package com.example.chatservice.kafka;

import com.example.chatservice.dto.ReadReceipt;
import com.example.chatservice.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaReadReceiptConsumer {
    private final ChatMessageService chatMessageService;

    @KafkaListener(topics = "read_receipts", groupId = "read-receipt-group")
    public void handleReadReceipt(ReadReceipt receipt) {
        //честно, это выглядит как очень багованнное место
        //как только появится фронт надо тестить со всех сил
        chatMessageService.markAsRead(receipt.getMessageId(), receipt.getRecipientId());
    }
}
