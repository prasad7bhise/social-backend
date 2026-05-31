package com.example.social.app.business.service.message.impl;

import com.example.social.app.business.dto.feed.UserBriefDTO;
import com.example.social.app.business.dto.message.ConversationDTO;
import com.example.social.app.business.dto.message.MessageDTO;
import com.example.social.app.business.dto.message.MessageRequestDTO;
import com.example.social.app.business.dto.messagereaction.MessageReactionDTO;
import com.example.social.app.business.mapper.FeedMapper;
import com.example.social.app.business.service.message.MessageService;
import com.example.social.app.business.service.notification.NotificationService;
import com.example.social.app.db.dao.conversation.ConversationParticipantRepository;
import com.example.social.app.db.dao.conversation.ConversationRepository;
import com.example.social.app.db.dao.follow.FollowRepository;
import com.example.social.app.db.dao.message.MessageRepository;
import com.example.social.app.db.dao.messagereaction.MessageReactionRepository;
import com.example.social.app.db.dao.messagerequest.MessageRequestRepository;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.conversation.ConversationEntity;
import com.example.social.app.db.entity.conversation.ConversationParticipantEntity;
import com.example.social.app.db.entity.message.MessageEntity;
import com.example.social.app.db.entity.messagerequest.MessageRequestEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import com.example.social.app.enums.MessageRequestStatus;
import com.example.social.app.enums.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final MessageRequestRepository messageRequestRepository;
    private final UsersRepository usersRepository;
    private final FollowRepository followRepository;
    private final FeedMapper feedMapper;
    private final NotificationService notificationService;
    private final MessageReactionRepository reactionRepository;

    @Override
    @Transactional
    public Map<String, Object> createOrGetConversation(String keycloakId, Long recipientId, String initialContent) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        UsersEntity recipient = usersRepository.findById(recipientId)
                .orElseThrow(() -> new EntityNotFoundException("Recipient not found"));

        if (currentUser.getId().equals(recipientId)) {
            throw new IllegalArgumentException("Cannot message yourself");
        }

        List<Long> commonIds = participantRepository.findCommonConversationIds(currentUser.getId(), recipientId);
        if (!commonIds.isEmpty()) {
            ConversationEntity existing = conversationRepository.findById(commonIds.get(0))
                    .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));
            ConversationDTO dto = buildConversationDTO(existing, currentUser);
            return Map.of("conversation", dto);
        }

        boolean isFollowing = followRepository.existsByFollowerAndFollowing(currentUser, recipient);
        if (isFollowing) {
            ConversationEntity conversation = new ConversationEntity();
            conversationRepository.save(conversation);

            ConversationParticipantEntity cp1 = new ConversationParticipantEntity();
            cp1.setConversation(conversation);
            cp1.setUser(currentUser);
            participantRepository.save(cp1);

            ConversationParticipantEntity cp2 = new ConversationParticipantEntity();
            cp2.setConversation(conversation);
            cp2.setUser(recipient);
            participantRepository.save(cp2);

            MessageEntity message = new MessageEntity();
            message.setConversation(conversation);
            message.setSender(currentUser);
            message.setContent(initialContent);
            messageRepository.save(message);

            notificationService.createNotification(
                    recipient.getId(), currentUser.getId(),
                    NotificationType.MESSAGE_ACCEPTED,
                    conversation.getId(),
                    currentUser.getFirstName() + " sent you a message"
            );

            ConversationDTO dto = buildConversationDTO(conversation, currentUser);
            return Map.of("conversation", dto);
        }

        Optional<MessageRequestEntity> existingPending = messageRequestRepository
                .findByFromUserIdAndToUserIdAndStatus(currentUser.getId(), recipient.getId(), MessageRequestStatus.PENDING);
        if (existingPending.isPresent()) {
            return Map.of("request", toRequestDTO(existingPending.get()));
        }

        MessageRequestEntity request = new MessageRequestEntity();
        request.setFromUser(currentUser);
        request.setToUser(recipient);
        request.setStatus(MessageRequestStatus.PENDING);
        messageRequestRepository.save(request);

        notificationService.createNotification(
                recipient.getId(), currentUser.getId(),
                NotificationType.MESSAGE_REQUEST,
                request.getId(),
                currentUser.getFirstName() + " wants to message you"
        );

        return Map.of("request", toRequestDTO(request));
    }

    @Override
    @Transactional
    public MessageDTO sendMessage(String keycloakId, Long conversationId, String content) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));

        participantRepository.findByConversationIdAndUserId(conversationId, currentUser.getId())
                .orElseThrow(() -> new SecurityException("Not a participant of this conversation"));

        List<ConversationParticipantEntity> participants = participantRepository.findByConversationId(conversationId);
        UsersEntity recipient = participants.stream()
                .map(ConversationParticipantEntity::getUser)
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);

        MessageEntity message = new MessageEntity();
        message.setConversation(conversation);
        message.setSender(currentUser);
        message.setContent(content);
        messageRepository.save(message);

        conversation.setUpdatedAt(message.getCreatedAt());
        conversationRepository.save(conversation);

        if (recipient != null && !recipient.getId().equals(currentUser.getId())) {
            notificationService.createNotification(
                    recipient.getId(), currentUser.getId(),
                    NotificationType.MESSAGE,
                    conversation.getId(),
                    currentUser.getFirstName() + " sent you a message"
            );
        }

        return toMessageDTO(message, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversations(String keycloakId) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<ConversationParticipantEntity> myParticipants = participantRepository.findByUserWithConversation(currentUser);
        return myParticipants.stream()
                .map(cp -> buildConversationDTO(cp.getConversation(), currentUser))
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageDTO> getMessages(String keycloakId, Long conversationId, int page, int size) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        participantRepository.findByConversationIdAndUserId(conversationId, currentUser.getId())
                .orElseThrow(() -> new SecurityException("Not a participant of this conversation"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .map(m -> toMessageDTO(m, currentUser.getId()));
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String keycloakId, Long conversationId) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        participantRepository.findByConversationIdAndUserId(conversationId, currentUser.getId())
                .orElseThrow(() -> new SecurityException("Not a participant of this conversation"));

        messageRepository.markAsRead(conversationId, currentUser.getId(), LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDTO getConversation(String keycloakId, Long conversationId) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));

        participantRepository.findByConversationIdAndUserId(conversationId, currentUser.getId())
                .orElseThrow(() -> new SecurityException("Not a participant of this conversation"));

        return buildConversationDTO(conversation, currentUser);
    }

    @Override
    @Transactional
    public MessageDTO updateMessage(String keycloakId, Long messageId, String content) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new SecurityException("You can only edit your own messages");
        }

        if (message.getCreatedAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
            throw new SecurityException("Messages can only be edited within 5 minutes of sending");
        }

        message.setContent(content);
        messageRepository.save(message);

        return toMessageDTO(message, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageRequestDTO> getMessageRequests(String keycloakId) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return messageRequestRepository
                .findByToUserIdAndStatusOrderByCreatedAtDesc(currentUser.getId(), MessageRequestStatus.PENDING)
                .stream()
                .map(this::toRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConversationDTO acceptMessageRequest(String keycloakId, Long requestId) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        MessageRequestEntity request = messageRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Message request not found"));

        if (!request.getToUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Cannot accept another user's message request");
        }

        request.setStatus(MessageRequestStatus.ACCEPTED);
        messageRequestRepository.save(request);

        ConversationEntity conversation = new ConversationEntity();
        conversationRepository.save(conversation);

        ConversationParticipantEntity cp1 = new ConversationParticipantEntity();
        cp1.setConversation(conversation);
        cp1.setUser(request.getFromUser());
        participantRepository.save(cp1);

        ConversationParticipantEntity cp2 = new ConversationParticipantEntity();
        cp2.setConversation(conversation);
        cp2.setUser(request.getToUser());
        participantRepository.save(cp2);

        MessageEntity message = new MessageEntity();
        message.setConversation(conversation);
        message.setSender(request.getFromUser());
        message.setContent("Message request accepted");
        messageRepository.save(message);

        notificationService.createNotification(
                request.getFromUser().getId(), currentUser.getId(),
                NotificationType.MESSAGE_ACCEPTED,
                conversation.getId(),
                currentUser.getFirstName() + " accepted your message request"
        );

        return buildConversationDTO(conversation, currentUser);
    }

    @Override
    @Transactional
    public void declineMessageRequest(String keycloakId, Long requestId) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        MessageRequestEntity request = messageRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Message request not found"));

        if (!request.getToUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Cannot decline another user's message request");
        }

        request.setStatus(MessageRequestStatus.DECLINED);
        messageRequestRepository.save(request);
    }

    private ConversationDTO buildConversationDTO(ConversationEntity conversation, UsersEntity currentUser) {
        List<ConversationParticipantEntity> participants = participantRepository.findByConversationId(conversation.getId());
        UsersEntity otherUser = participants.stream()
                .map(ConversationParticipantEntity::getUser)
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);

        UserBriefDTO participantBrief = otherUser != null ? feedMapper.toUserBrief(otherUser) : null;

        Optional<MessageEntity> lastMsg = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId());
        MessageDTO lastMessageDTO = lastMsg.map(m -> toMessageDTO(m, currentUser.getId())).orElse(null);

        long unreadCount = messageRepository.countByConversationIdAndReadAtIsNullAndSenderIdNot(
                conversation.getId(), currentUser.getId());

        ConversationDTO dto = new ConversationDTO();
        dto.setId(conversation.getId());
        dto.setParticipant(participantBrief);
        dto.setLastMessage(lastMessageDTO);
        dto.setUnreadCount(unreadCount);
        dto.setUpdatedAt(conversation.getUpdatedAt());
        return dto;
    }

    private MessageDTO toMessageDTO(MessageEntity entity, Long currentUserId) {
        UserBriefDTO senderBrief = feedMapper.toUserBrief(entity.getSender());
        MessageDTO dto = new MessageDTO();
        dto.setId(entity.getId());
        dto.setConversationId(entity.getConversation().getId());
        dto.setSender(senderBrief);
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setReadAt(entity.getReadAt());
        dto.setRead(entity.getReadAt() != null);
        dto.setEditable(
                entity.getSender().getId().equals(currentUserId) &&
                entity.getCreatedAt().plusMinutes(5).isAfter(LocalDateTime.now())
        );
        dto.setReactions(
                reactionRepository.findByMessageId(entity.getId())
                        .stream()
                        .map(r -> new MessageReactionDTO(r.getId(), r.getUser().getId(), r.getEmoji(), r.getUser().getFirstName()))
                        .toList()
        );
        return dto;
    }

    private MessageRequestDTO toRequestDTO(MessageRequestEntity entity) {
        UserBriefDTO fromBrief = feedMapper.toUserBrief(entity.getFromUser());
        UserBriefDTO toBrief = feedMapper.toUserBrief(entity.getToUser());
        MessageRequestDTO dto = new MessageRequestDTO();
        dto.setId(entity.getId());
        dto.setFromUser(fromBrief);
        dto.setToUser(toBrief);
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
