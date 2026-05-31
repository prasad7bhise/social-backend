package com.example.social.business.service.notification;

import com.example.social.app.business.dto.feed.UserBriefDTO;
import com.example.social.app.business.dto.notification.NotificationDTO;
import com.example.social.app.business.mapper.FeedMapper;
import com.example.social.app.business.service.notification.impl.NotificationServiceImpl;
import com.example.social.app.db.dao.notification.NotificationRepository;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.notification.NotificationEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import com.example.social.app.enums.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static com.example.social.business.service.notification.dataset.NotificationServiceImplDataset.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplUnitTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private FeedMapper feedMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UsersEntity recipient;
    private UsersEntity actor;
    private NotificationEntity unreadNotif;

    @BeforeEach
    void setUp() {
        recipient = recipient();
        actor = actor();
        unreadNotif = unreadNotification();
    }

    @Test
    void test01_createNotification_shouldSaveNotification() {
        when(usersRepository.findById(1L)).thenReturn(Optional.of(recipient));
        when(usersRepository.findById(2L)).thenReturn(Optional.of(actor));

        notificationService.createNotification(1L, 2L, NotificationType.LIKE, 10L, "Actor User liked your post");

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());
        NotificationEntity saved = captor.getValue();
        assertThat(saved.getRecipient().getId()).isEqualTo(1L);
        assertThat(saved.getActor().getId()).isEqualTo(2L);
        assertThat(saved.getType()).isEqualTo(NotificationType.LIKE);
        assertThat(saved.getReferenceId()).isEqualTo(10L);
        assertThat(saved.getContent()).isEqualTo("Actor User liked your post");
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    void test02_createNotification_shouldThrowEntityNotFoundException_whenRecipientNotFound() {
        when(usersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.createNotification(99L, 2L, NotificationType.LIKE, null, "test"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Recipient not found");
    }

    @Test
    void test03_getNotifications_shouldReturnNotifications() {
        when(usersRepository.findByKeycloakId("kc-recipient")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(unreadNotif)));

        UserBriefDTO actorBrief = new UserBriefDTO();
        actorBrief.setId(2L);
        actorBrief.setFirstName("Actor");
        when(feedMapper.toUserBrief(actor)).thenReturn(actorBrief);

        Page<NotificationDTO> result = notificationService.getNotifications("kc-recipient", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo(NotificationType.LIKE);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Actor User liked your post");
    }

    @Test
    void test04_getUnreadCount_shouldReturnCount() {
        when(usersRepository.findByKeycloakId("kc-recipient")).thenReturn(Optional.of(recipient));
        when(notificationRepository.countByRecipientIdAndIsReadFalse(1L)).thenReturn(3L);

        long count = notificationService.getUnreadCount("kc-recipient");

        assertThat(count).isEqualTo(3);
    }

    @Test
    void test05_markAsRead_shouldMarkNotificationRead() {
        when(usersRepository.findByKeycloakId("kc-recipient")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(unreadNotif));

        notificationService.markAsRead("kc-recipient", 100L);

        assertThat(unreadNotif.isRead()).isTrue();
        verify(notificationRepository).save(unreadNotif);
    }

    @Test
    void test06_markAsRead_shouldThrowSecurityException_whenNotOwnNotification() {
        UsersEntity otherUser = new UsersEntity();
        otherUser.setId(99L);
        otherUser.setKeycloakId("kc-other");

        when(usersRepository.findByKeycloakId("kc-other")).thenReturn(Optional.of(otherUser));
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(unreadNotif));

        assertThatThrownBy(() -> notificationService.markAsRead("kc-other", 100L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Cannot mark another user's notification as read");
    }

    @Test
    void test07_markAllAsRead_shouldMarkAllRead() {
        when(usersRepository.findByKeycloakId("kc-recipient")).thenReturn(Optional.of(recipient));

        notificationService.markAllAsRead("kc-recipient");

        verify(notificationRepository).markAllAsRead(1L);
    }

    @Test
    void test08_getNotifications_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(usersRepository.findByKeycloakId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotifications("unknown", 0, 10))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }
}
