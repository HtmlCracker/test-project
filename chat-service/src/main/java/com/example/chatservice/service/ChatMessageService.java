package com.example.chatservice.service;

import com.example.chatservice.client.ProfileClient;
import com.example.chatservice.dto.ChatDto;
import com.example.chatservice.dto.ChatParticipantDto;
import com.example.chatservice.dto.Message;
import com.example.chatservice.dto.ProfileDto;
import com.example.chatservice.entity.Chat;
import com.example.chatservice.entity.ChatMember;
import com.example.chatservice.entity.ChatMessage;
import com.example.chatservice.entity.MessageReadStatus;
import com.example.chatservice.repository.ChatMemberRepository;
import com.example.chatservice.repository.ChatMessageRepository;
import com.example.chatservice.repository.ChatRepository;
import com.example.chatservice.repository.MessageReadStatusRepository;
import com.example.chatservice.state.ChatType;
import com.example.chatservice.state.MessageState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final MessageReadStatusRepository readStatusRepository;
    private final ProfileClient profileClient;

    public UUID createChat(List<UUID> participantIds, String name, ChatType type, UUID createdBy) {
        List<ProfileDto> members = profileClient.getProfilesByUserIds(participantIds);
        if (members.size() != participantIds.size()) {
            throw new IllegalArgumentException("One or more users not found");
        }

        Chat chat = new Chat();
        chat.setType(type);
        chat.setName(name);
        chat.setCreatedAt(LocalDateTime.now());
        chatRepository.save(chat);

        for (UUID participantId : participantIds) {
            ChatMember member = new ChatMember();
            member.setChat(chat);
            member.setUserId(participantId);
            member.setJoinedAt(LocalDateTime.now());
            chatMemberRepository.save(member);
        }

        return chat.getId();
    }

    public ChatMessage sendMessage(UUID chatId, UUID senderId, String text) {
        if (!isChatParticipant(chatId, senderId)) {
            throw new IllegalArgumentException("User is not a participant of the chat");
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setChatId(chatId);
        message.setText(text);
        message.setTimestamp(LocalDateTime.now());
        message.setEdited(false);
        message.setStatus(MessageState.SENT);

        return saveMessage(message);
    }

    public ChatMessage saveMessage(Message message) {
        ChatMessage chatMessage = toChatMessage(message);
        message.setStatus(MessageState.DELIVERED);
       return chatMessageRepository.save(chatMessage);
    }

    public Page<Message> getChatHistory(UUID chatId, UUID userId, int page, int size) {
        if (!isChatParticipant(chatId, userId)) {
            throw new IllegalArgumentException("User is not a participant of the chat");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<ChatMessage> chatMessages = chatMessageRepository.findByChatId(chatId, pageable);
        return chatMessages.map(this::toMessage);
    }

    public ChatDto getChatInfo(UUID chatId, UUID requestingUserId) {
        if (!isChatParticipant(chatId, requestingUserId)) {
            throw new IllegalArgumentException("User is not a participant of the chat");
        }

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        Message lastMessage = chatMessageRepository.findTopByChatIdOrderByTimestampDesc(chatId)
                .map(this::toMessage)
                .orElse(null);

        List<ChatParticipantDto> participants = getChatParticipants(chatId, requestingUserId);

        ChatDto chatInfo = new ChatDto();
        chatInfo.setId(chat.getId());
        chatInfo.setType(chat.getType());
        chatInfo.setName(chat.getName());
        chatInfo.setCreatedAt(chat.getCreatedAt());
        chatInfo.setParticipants(participants);
        chatInfo.setLastMessage(lastMessage);

        return chatInfo;
    }

    public List<ChatParticipantDto> getChatParticipants(UUID chatId, UUID requestingUserId) {
        if (!isChatParticipant(chatId, requestingUserId)) {
            throw new IllegalArgumentException("User is not a participant of the chat");
        }

        List<ChatMember> chatMembers = chatMemberRepository.findByChatId(chatId);
        List<UUID> userIds = chatMembers.stream()
                .map(ChatMember::getUserId)
                .collect(Collectors.toList());

        List<ProfileDto> memberDtos = profileClient.getProfilesByUserIds(userIds);

        // Объединение информации
        return chatMembers.stream()
                .map(chatMember -> {
                    ProfileDto profileDto = memberDtos.stream()
                            .filter(dto -> dto.getId().equals(chatMember.getUserId()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Member info not found"));

                    ChatParticipantDto participantInfo = new ChatParticipantDto();
                    participantInfo.setUserId(chatMember.getUserId());
                    participantInfo.setName(profileDto.getName());
                    participantInfo.setSurname(profileDto.getSurname());
                    participantInfo.setEmail(profileDto.getEmail());
                    participantInfo.setJoinedAt(chatMember.getJoinedAt());
                    return participantInfo;
                })
                .collect(Collectors.toList());
    }

    public List<ChatDto> getUserChats(UUID userId) {
        List<Chat> chats = chatRepository.findByParticipantId(userId);
        return chats.stream()
                .map(chat -> getChatInfo(chat.getId(), userId))
                .collect(Collectors.toList());
    }

    public void addParticipant(UUID chatId, UUID adminId, UUID newParticipantId) {
        if (!isChatParticipant(chatId, adminId)) {
            throw new IllegalArgumentException("User is not a participant of the chat");
        }

        profileClient.getProfileDto(newParticipantId);

        if (isChatParticipant(chatId, newParticipantId)) {
            throw new IllegalArgumentException("User is already a participant of the chat");
        }

        ChatMember member = new ChatMember();
        member.setChat(chatRepository.findById(chatId).orElse(null));
        member.setUserId(newParticipantId);
        member.setJoinedAt(LocalDateTime.now());
        chatMemberRepository.save(member);
    }

    public void removeParticipant(UUID chatId, UUID adminId, UUID participantId) {
        //todo роли
        if (!isChatParticipant(chatId, adminId)) {
            throw new IllegalArgumentException("User is not a participant of the chat");
        }

        if (!isChatParticipant(chatId, participantId)) {
            throw new IllegalArgumentException("User is not a participant of the chat");
        }

        chatMemberRepository.deleteByChatIdAndUserId(chatId, participantId);
    }

    public void markAsRead(UUID messageId, UUID userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!isChatParticipant(message.getChatId(), userId)) {
            throw new IllegalArgumentException("User is not a participant of the chat");
        }

        MessageReadStatus readStatus = readStatusRepository.findByMessageIdAndUserId(messageId, userId)
                .orElseGet(() -> {
                    MessageReadStatus newStatus = new MessageReadStatus();
                    newStatus.setMessageId(messageId);
                    newStatus.setUserId(userId);
                    return newStatus;
                });

        readStatus.setReadAt(LocalDateTime.now());
        readStatusRepository.save(readStatus);
    }

    public void markAsDelivered(UUID messageId) {
        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        chatMessage.setStatus(MessageState.DELIVERED);
        chatMessageRepository.save(chatMessage);
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

    public ChatMessage findById(UUID messageId) {
        return chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
    }

    public List<Message> getUndeliveredMessages(UUID userId) {
        //todo сделать с момента когда последний раз был онлайн
        //вообще эта хуйня с онлайном должна быть в profile-service, но я хз как это сделать
        //господи боже помоги!!!
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        return chatMessageRepository.findUndeliveredMessagesForUser(userId, twentyFourHoursAgo).stream().map(this::toMessage).collect(Collectors.toList());
    }

    private ChatMessage toChatMessage(Message message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(message.getSenderId());
        chatMessage.setChatId(message.getChatId());
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
        message.setChatId(chatMessage.getChatId());
        message.setText(chatMessage.getText());
        message.setTimestamp(chatMessage.getTimestamp());
        message.setStatus(chatMessage.getStatus());
        message.setReadAt(chatMessage.getReadAt());
        message.setEdited(chatMessage.isEdited());
        return message;
    }

    private boolean isChatParticipant(UUID chatId, UUID userId) {
        return chatMemberRepository.existsByChatIdAndUserId(chatId, userId); //todo КЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭЭШ!!!!!!!!!!!!!!!!!!!!!
    }


}
