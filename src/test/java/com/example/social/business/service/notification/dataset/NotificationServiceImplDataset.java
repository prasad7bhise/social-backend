package com.example.social.business.service.notification.dataset;

import com.example.social.app.business.dto.feed.UserBriefDTO;
import com.example.social.app.business.dto.notification.NotificationDTO;
import com.example.social.app.db.entity.notification.NotificationEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import com.example.social.app.enums.NotificationType;

import java.time.LocalDateTime;

public class NotificationServiceImplDataset {

    public static UsersEntity recipient() {
        UsersEntity u = new UsersEntity();
        u.setId(1L);
        u.setKeycloakId("kc-recipient");
        u.setFirstName("Recipient");
        u.setLastName("User");
        u.setEmail("recipient@example.com");
        u.setRole("USER");
        return u;
    }

    public static UsersEntity actor() {
        UsersEntity u = new UsersEntity();
        u.setId(2L);
        u.setKeycloakId("kc-actor");
        u.setFirstName("Actor");
        u.setLastName("User");
        u.setEmail("actor@example.com");
        u.setRole("USER");
        return u;
    }

    public static NotificationEntity unreadNotification() {
        NotificationEntity n = new NotificationEntity();
        n.setId(100L);
        n.setRecipient(recipient());
        n.setActor(actor());
        n.setType(NotificationType.LIKE);
        n.setReferenceId(10L);
        n.setContent("Actor User liked your post");
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now().minusHours(1));
        return n;
    }

    public static NotificationEntity readNotification() {
        NotificationEntity n = new NotificationEntity();
        n.setId(101L);
        n.setRecipient(recipient());
        n.setActor(actor());
        n.setType(NotificationType.FOLLOW);
        n.setReferenceId(null);
        n.setContent("Actor User started following you");
        n.setRead(true);
        n.setCreatedAt(LocalDateTime.now().minusDays(1));
        return n;
    }

    public static NotificationDTO unreadNotificationDTO() {
        UserBriefDTO actorBrief = new UserBriefDTO();
        actorBrief.setId(2L);
        actorBrief.setFirstName("Actor");
        actorBrief.setLastName("User");

        NotificationDTO d = new NotificationDTO();
        d.setId(100L);
        d.setType(NotificationType.LIKE);
        d.setActor(actorBrief);
        d.setReferenceId(10L);
        d.setContent("Actor User liked your post");
        d.setRead(false);
        d.setCreatedAt(LocalDateTime.now().minusHours(1));
        return d;
    }
}
