package com.example.social.app.business.service.notification.impl;

import com.example.social.app.business.dto.feed.UserBriefDTO;
import com.example.social.app.business.dto.notification.NotificationDTO;
import com.example.social.app.business.mapper.FeedMapper;
import com.example.social.app.business.service.notification.NotificationService;
import com.example.social.app.db.dao.notification.NotificationRepository;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.notification.NotificationEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import com.example.social.app.enums.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UsersRepository usersRepository;
    private final FeedMapper feedMapper;

    @Override
    @Transactional
    public void createNotification(Long recipientId, Long actorId, NotificationType type, Long referenceId, String content) {
        UsersEntity recipient = usersRepository.findById(recipientId)
                .orElseThrow(() -> new EntityNotFoundException("Recipient not found"));
        UsersEntity actor = usersRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("Actor not found"));

        NotificationEntity notification = new NotificationEntity();
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setContent(content);
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotifications(String keycloakId, int page, int size) {
        UsersEntity user = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String keycloakId) {
        UsersEntity user = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
    }

    @Override
    @Transactional
    public void markAsRead(String keycloakId, Long notificationId) {
        UsersEntity user = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new SecurityException("Cannot mark another user's notification as read");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String keycloakId) {
        UsersEntity user = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        notificationRepository.markAllAsRead(user.getId());
    }

    private NotificationDTO toDTO(NotificationEntity entity) {
        UserBriefDTO actorBrief = feedMapper.toUserBrief(entity.getActor());
        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setType(entity.getType());
        dto.setActor(actorBrief);
        dto.setReferenceId(entity.getReferenceId());
        dto.setContent(entity.getContent());
        dto.setRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
