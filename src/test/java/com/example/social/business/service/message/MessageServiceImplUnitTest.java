package com.example.social.business.service.message;

import com.example.social.app.business.dto.feed.UserBriefDTO;
import com.example.social.app.business.dto.message.ConversationDTO;
import com.example.social.app.business.dto.message.MessageDTO;
import com.example.social.app.business.dto.message.MessageRequestDTO;
import com.example.social.app.business.mapper.FeedMapper;
import com.example.social.app.business.service.message.impl.MessageServiceImpl;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.social.business.service.message.dataset.MessageServiceImplDataset.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplUnitTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationParticipantRepository participantRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageRequestRepository messageRequestRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private FeedMapper feedMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MessageReactionRepository reactionRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    private UsersEntity currentUser;
    private UsersEntity recipient;
    private ConversationEntity conversation;
    private MessageEntity sentMsg;

    @BeforeEach
    void setUp() {
        currentUser = currentUser();
        recipient = recipient();
        conversation = existingConversation();
        sentMsg = sentMessage();
    }

    @Test
    void test01_createOrGetConversation_shouldReturnExistingConversation() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(usersRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(participantRepository.findCommonConversationIds(1L, 2L)).thenReturn(List.of(100L));
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));
        when(participantRepository.findByConversationId(100L)).thenReturn(bothParticipants());
        when(feedMapper.toUserBrief(recipient)).thenReturn(recipientBrief());
        when(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.empty());
        when(messageRepository.countByConversationIdAndReadAtIsNullAndSenderIdNot(100L, 1L)).thenReturn(0L);

        Map<String, Object> result = messageService.createOrGetConversation("kc-current", 2L, "Hello");

        assertThat(result).containsKey("conversation");
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void test02_createOrGetConversation_shouldCreateNewConversation_whenFollowing() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(usersRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(participantRepository.findCommonConversationIds(1L, 2L)).thenReturn(List.of());
        when(followRepository.existsByFollowerAndFollowing(currentUser, recipient)).thenReturn(true);
        when(conversationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantRepository.findByConversationId(any())).thenReturn(bothParticipants());
        when(feedMapper.toUserBrief(recipient)).thenReturn(recipientBrief());
        when(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(any())).thenReturn(Optional.empty());
        when(messageRepository.countByConversationIdAndReadAtIsNullAndSenderIdNot(any(), anyLong())).thenReturn(0L);

        Map<String, Object> result = messageService.createOrGetConversation("kc-current", 2L, "Hello");

        assertThat(result).containsKey("conversation");
        verify(conversationRepository, times(1)).save(any());
        verify(participantRepository, times(2)).save(any());
        verify(messageRepository, times(1)).save(any());
        verify(notificationService).createNotification(
                2L, 1L, NotificationType.MESSAGE_ACCEPTED, null, "Current sent you a message"
        );
    }

    @Test
    void test03_createOrGetConversation_shouldReturnPendingRequest_whenExists() {
        MessageRequestEntity pending = pendingRequest();
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(usersRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(participantRepository.findCommonConversationIds(1L, 2L)).thenReturn(List.of());
        when(followRepository.existsByFollowerAndFollowing(currentUser, recipient)).thenReturn(false);
        when(messageRequestRepository.findByFromUserIdAndToUserIdAndStatus(1L, 2L, MessageRequestStatus.PENDING))
                .thenReturn(Optional.of(pending));
        when(feedMapper.toUserBrief(currentUser)).thenReturn(currentUserBrief());
        when(feedMapper.toUserBrief(recipient)).thenReturn(recipientBrief());

        Map<String, Object> result = messageService.createOrGetConversation("kc-current", 2L, "Hello");

        assertThat(result).containsKey("request");
        verify(messageRequestRepository, never()).save(any());
    }

    @Test
    void test04_createOrGetConversation_shouldCreateNewMessageRequest() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(usersRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(participantRepository.findCommonConversationIds(1L, 2L)).thenReturn(List.of());
        when(followRepository.existsByFollowerAndFollowing(currentUser, recipient)).thenReturn(false);
        when(messageRequestRepository.findByFromUserIdAndToUserIdAndStatus(1L, 2L, MessageRequestStatus.PENDING))
                .thenReturn(Optional.empty());
        when(messageRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(feedMapper.toUserBrief(currentUser)).thenReturn(currentUserBrief());
        when(feedMapper.toUserBrief(recipient)).thenReturn(recipientBrief());

        Map<String, Object> result = messageService.createOrGetConversation("kc-current", 2L, "Hello");

        assertThat(result).containsKey("request");
        verify(messageRequestRepository).save(any());
        verify(notificationService).createNotification(
                2L, 1L, NotificationType.MESSAGE_REQUEST, null, "Current wants to message you"
        );
    }

    @Test
    void test05_createOrGetConversation_shouldThrowException_whenMessagingSelf() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(usersRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> messageService.createOrGetConversation("kc-current", 1L, "Hello"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot message yourself");
    }

    @Test
    void test06_sendMessage_shouldSendAndNotify() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));
        when(participantRepository.findByConversationIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(currentParticipant()));
        when(participantRepository.findByConversationId(100L)).thenReturn(bothParticipants());
        when(messageRepository.save(any())).thenAnswer(inv -> {
            MessageEntity m = inv.getArgument(0);
            if (m.getCreatedAt() == null) m.setCreatedAt(LocalDateTime.now());
            return m;
        });

        MessageDTO mockDto = messageDTO();
        // Mock toMessageDTO dependencies
        when(feedMapper.toUserBrief(currentUser)).thenReturn(currentUserBrief());
        when(reactionRepository.findByMessageId(any())).thenReturn(List.of());

        MessageDTO result = messageService.sendMessage("kc-current", 100L, "Hello!");

        assertThat(result).isNotNull();
        verify(conversationRepository).save(conversation);
        verify(notificationService).createNotification(
                2L, 1L, NotificationType.MESSAGE, 100L, "Current sent you a message"
        );
    }

    @Test
    void test07_sendMessage_shouldThrowSecurityException_whenNotParticipant() {
        when(usersRepository.findByKeycloakId("other")).thenReturn(Optional.of(currentUser));
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));
        when(participantRepository.findByConversationIdAndUserId(100L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.sendMessage("other", 100L, "Hello"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Not a participant of this conversation");
    }

    @Test
    void test08_getConversations_shouldReturnSortedConversations() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        ConversationParticipantEntity cp = currentParticipant();
        when(participantRepository.findByUserWithConversation(currentUser)).thenReturn(List.of(cp));
        when(participantRepository.findByConversationId(100L)).thenReturn(bothParticipants());
        when(feedMapper.toUserBrief(recipient)).thenReturn(recipientBrief());
        when(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.empty());
        when(messageRepository.countByConversationIdAndReadAtIsNullAndSenderIdNot(100L, 1L)).thenReturn(0L);

        List<ConversationDTO> result = messageService.getConversations("kc-current");

        assertThat(result).hasSize(1);
    }

    @Test
    void test09_getMessages_shouldReturnMessages() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(participantRepository.findByConversationIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(currentParticipant()));
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(eq(100L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(sentMsg)));
        when(feedMapper.toUserBrief(currentUser)).thenReturn(currentUserBrief());
        when(reactionRepository.findByMessageId(any())).thenReturn(List.of());

        Page<MessageDTO> result = messageService.getMessages("kc-current", 100L, 0, 20);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void test10_markMessagesAsRead_shouldMarkRead() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(participantRepository.findByConversationIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(currentParticipant()));

        messageService.markMessagesAsRead("kc-current", 100L);

        verify(messageRepository).markAsRead(eq(100L), eq(1L), any(LocalDateTime.class));
    }

    @Test
    void test11_getConversation_shouldReturnConversation() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));
        when(participantRepository.findByConversationIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(currentParticipant()));
        when(participantRepository.findByConversationId(100L)).thenReturn(bothParticipants());
        when(feedMapper.toUserBrief(recipient)).thenReturn(recipientBrief());
        when(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.empty());
        when(messageRepository.countByConversationIdAndReadAtIsNullAndSenderIdNot(100L, 1L)).thenReturn(0L);

        ConversationDTO result = messageService.getConversation("kc-current", 100L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void test12_updateMessage_shouldUpdateOwnMessage() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(messageRepository.findById(200L)).thenReturn(Optional.of(sentMsg));
        when(messageRepository.save(any())).thenReturn(sentMsg);
        when(feedMapper.toUserBrief(currentUser)).thenReturn(currentUserBrief());
        when(reactionRepository.findByMessageId(any())).thenReturn(List.of());

        MessageDTO result = messageService.updateMessage("kc-current", 200L, "Edited!");

        assertThat(result).isNotNull();
        assertThat(sentMsg.getContent()).isEqualTo("Edited!");
    }

    @Test
    void test13_updateMessage_shouldThrowSecurityException_whenNotOwnMessage() {
        MessageEntity otherMsg = receivedMessage();
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(messageRepository.findById(201L)).thenReturn(Optional.of(otherMsg));

        assertThatThrownBy(() -> messageService.updateMessage("kc-current", 201L, "Hack"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("You can only edit your own messages");
    }

    @Test
    void test14_updateMessage_shouldThrowSecurityException_whenEditWindowExpired() {
        MessageEntity oldMsg = sentMessage();
        oldMsg.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(messageRepository.findById(200L)).thenReturn(Optional.of(oldMsg));

        assertThatThrownBy(() -> messageService.updateMessage("kc-current", 200L, "Late edit"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Messages can only be edited within 5 minutes of sending");
    }

    @Test
    void test15_getMessageRequests_shouldReturnPendingRequests() {
        MessageRequestEntity pending = pendingRequest();
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(messageRequestRepository.findByToUserIdAndStatusOrderByCreatedAtDesc(1L, MessageRequestStatus.PENDING))
                .thenReturn(List.of(pending));
        when(feedMapper.toUserBrief(currentUser)).thenReturn(currentUserBrief());
        when(feedMapper.toUserBrief(recipient)).thenReturn(recipientBrief());

        List<MessageRequestDTO> result = messageService.getMessageRequests("kc-current");

        assertThat(result).hasSize(1);
    }

    @Test
    void test16_acceptMessageRequest_shouldCreateConversation() {
        MessageRequestEntity pending = pendingRequest();
        when(usersRepository.findByKeycloakId("kc-recipient")).thenReturn(Optional.of(recipient));
        when(messageRequestRepository.findById(300L)).thenReturn(Optional.of(pending));

        // pending request has fromUser=currentUser, toUser=recipient
        // but recipient is the one accepting (kc-recipient -> recipient -> id=2)
        // request's toUser.id should match current user
        pending.setToUser(recipient);

        when(conversationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participantRepository.findByConversationId(any())).thenReturn(bothParticipants());
        when(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(any())).thenReturn(Optional.empty());
        when(messageRepository.countByConversationIdAndReadAtIsNullAndSenderIdNot(any(), anyLong())).thenReturn(0L);

        ConversationDTO result = messageService.acceptMessageRequest("kc-recipient", 300L);

        assertThat(result).isNotNull();
        assertThat(pending.getStatus()).isEqualTo(MessageRequestStatus.ACCEPTED);
        verify(notificationService).createNotification(
                1L, 2L, NotificationType.MESSAGE_ACCEPTED, null, "Recipient accepted your message request"
        );
    }

    @Test
    void test17_acceptMessageRequest_shouldThrowSecurityException_whenNotOwnRequest() {
        MessageRequestEntity pending = pendingRequest();
        pending.setToUser(recipient);
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(messageRequestRepository.findById(300L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> messageService.acceptMessageRequest("kc-current", 300L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Cannot accept another user's message request");
    }

    @Test
    void test18_declineMessageRequest_shouldDecline() {
        MessageRequestEntity pending = pendingRequest();
        pending.setToUser(recipient);
        when(usersRepository.findByKeycloakId("kc-recipient")).thenReturn(Optional.of(recipient));
        when(messageRequestRepository.findById(300L)).thenReturn(Optional.of(pending));

        messageService.declineMessageRequest("kc-recipient", 300L);

        assertThat(pending.getStatus()).isEqualTo(MessageRequestStatus.DECLINED);
        verify(messageRequestRepository).save(pending);
    }

    @Test
    void test19_declineMessageRequest_shouldThrowSecurityException_whenNotOwnRequest() {
        MessageRequestEntity pending = pendingRequest();
        pending.setToUser(recipient);
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(messageRequestRepository.findById(300L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> messageService.declineMessageRequest("kc-current", 300L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Cannot decline another user's message request");
    }

    @Test
    void test20_getMessages_shouldThrowSecurityException_whenNotParticipant() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(participantRepository.findByConversationIdAndUserId(100L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.getMessages("kc-current", 100L, 0, 20))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Not a participant of this conversation");
    }
}
