package com.example.social.app.business.service.notification;

import com.example.social.app.business.dto.notification.NotificationDTO;
import com.example.social.app.enums.NotificationType;
import org.springframework.data.domain.Page;

public interface NotificationService {

    void createNotification(Long recipientId, Long actorId, NotificationType type, Long referenceId, String content);

    Page<NotificationDTO> getNotifications(String keycloakId, int page, int size);

    long getUnreadCount(String keycloakId);

    void markAsRead(String keycloakId, Long notificationId);

    void markAllAsRead(String keycloakId);
}
