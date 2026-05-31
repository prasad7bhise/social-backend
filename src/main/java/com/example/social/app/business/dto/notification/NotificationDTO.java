package com.example.social.app.business.dto.notification;

import com.example.social.app.business.dto.feed.UserBriefDTO;
import com.example.social.app.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private NotificationType type;
    private UserBriefDTO actor;
    private Long referenceId;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
}
