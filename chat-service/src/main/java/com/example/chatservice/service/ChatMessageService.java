package com.example.chatservice.service;

import com.example.chatservice.client.ProfileClient;
import com.example.chatservice.dto.Message;
import com.example.chatservice.entity.ChatMessage;
import com.example.chatservice.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ProfileClient profileClient;

    public void saveMessage(Message message) {
        validateRecipient(message.getRecipientId());
        ChatMessage chatMessage = toChatMessage(message);
        chatMessageRepository.save(chatMessage);
    }

    public List<Message> getChatHistory(UUID senderId, UUID recipientId) {
        validateRecipient(senderId);
        validateRecipient(recipientId);
        List<ChatMessage>  chatMessages = chatMessageRepository.findBySenderIdAndRecipientId(senderId, recipientId);
        return chatMessages.stream().map(this::toMessage).collect(Collectors.toList());
    }

    public void markAsRead(UUID senderId, UUID recipientId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findBySenderIdAndRecipientId(senderId, recipientId);
        for (ChatMessage chatMessage : chatMessages) {
            chatMessage.setRead(true);
            chatMessage.setReadAt(LocalDateTime.now());
            chatMessageRepository.save(chatMessage);
        }
    }

    private ChatMessage toChatMessage(Message message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(message.getSenderId());
        chatMessage.setRecipientId(message.getRecipientId());
        chatMessage.setText(message.getText());
        chatMessage.setTimestamp(message.getTimestamp());
        chatMessage.setRead(false);
        chatMessage.setStatus(message.getStatus());
        return chatMessage;
    }

    private Message toMessage(ChatMessage chatMessage) {
        Message message = new Message();
        message.setSenderId(chatMessage.getSenderId());
        message.setRecipientId(chatMessage.getRecipientId());
        message.setText(chatMessage.getText());
        message.setTimestamp(chatMessage.getTimestamp());
        return message;
    }

    private void validateRecipient(UUID recipientId) {
        try{
            profileClient.getProfileDto(recipientId); //todo КЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭШ!!!!!!!!!!!!!!!!!!!!!
        } catch(Exception e){
            throw new IllegalArgumentException("Recipient not found");
        }
    }
}
