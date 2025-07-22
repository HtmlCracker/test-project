package com.example.chatservice.service;

import com.example.chatservice.client.ProfileClient;
import com.example.chatservice.dto.Message;
import com.example.chatservice.entity.ChatMessage;
import com.example.chatservice.repository.ChatMessageRepository;
import com.example.chatservice.state.MessageState;
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

    public ChatMessage saveMessage(Message message) {
        validateRecipient(message.getRecipientId());
        ChatMessage chatMessage = toChatMessage(message);
       return chatMessageRepository.save(chatMessage);
    }

    public List<Message> getChatHistory(UUID senderId, UUID recipientId) {
        validateRecipient(senderId);
        validateRecipient(recipientId);
        markAllAsRead(senderId, recipientId);
        List<ChatMessage>  chatMessages = chatMessageRepository.findBySenderIdAndRecipientId(senderId, recipientId);
        return chatMessages.stream().map(this::toMessage).collect(Collectors.toList());
    }

    public void markAsDelivered(UUID messageId) {
        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        chatMessage.setStatus(MessageState.DELIVERED);
        chatMessageRepository.save(chatMessage);
    }

    public void markAsRead(UUID messageId, UUID recipientId) {
        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!chatMessage.getRecipientId().equals(recipientId)) {
            throw new IllegalArgumentException("User is not recipient");
        }

        chatMessage.setStatus(MessageState.READ);
        chatMessage.setReadAt(LocalDateTime.now());
        chatMessageRepository.save(chatMessage);
    }
    public void markAllAsRead(UUID senderId, UUID recipientId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findBySenderIdAndRecipientId(senderId, recipientId);
        for (ChatMessage chatMessage : chatMessages) {
            if (!chatMessage.getRecipientId().equals(recipientId)) {
                chatMessage.setStatus(MessageState.READ);
                chatMessage.setReadAt(LocalDateTime.now());
                chatMessageRepository.save(chatMessage);
            }
        }
    }

    public void markAsFailed(UUID  messageId) {
        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        chatMessage.setStatus(MessageState.FAILED);
        chatMessageRepository.save(chatMessage);
    }

    public Message editMessage(UUID messageId, UUID userId, String newText) {
        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!chatMessage.getSenderId().equals(userId)) {
            throw new IllegalArgumentException("Only author can edit message");
        }

        chatMessage.setText(newText);
        chatMessage.setEdited(true);
        return toMessage(chatMessageRepository.save(chatMessage));
    }

    public void deleteMessage(UUID messageId, UUID senderId) {
        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!chatMessage.getSenderId().equals(senderId)) {
            throw new IllegalArgumentException("User is not sender");
        }

        chatMessageRepository.deleteById(messageId);
    }

    private ChatMessage toChatMessage(Message message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(message.getSenderId());
        chatMessage.setRecipientId(message.getRecipientId());
        chatMessage.setText(message.getText());
        chatMessage.setTimestamp(message.getTimestamp());
        chatMessage.setStatus(message.getStatus());
        chatMessage.setReadAt(message.getReadAt());
        chatMessage.setEdited(false);
        return chatMessage;
    }

    private Message toMessage(ChatMessage chatMessage) {
        Message message = new Message();
        message.setSenderId(chatMessage.getSenderId());
        message.setRecipientId(chatMessage.getRecipientId());
        message.setText(chatMessage.getText());
        message.setTimestamp(chatMessage.getTimestamp());
        message.setStatus(chatMessage.getStatus());
        message.setReadAt(chatMessage.getReadAt());
        message.setEdited(chatMessage.isEdited());
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
