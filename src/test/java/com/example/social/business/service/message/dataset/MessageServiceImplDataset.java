package com.example.social.business.service.message.dataset;

import com.example.social.app.business.dto.feed.UserBriefDTO;
import com.example.social.app.business.dto.message.ConversationDTO;
import com.example.social.app.business.dto.message.MessageDTO;
import com.example.social.app.business.dto.message.MessageRequestDTO;
import com.example.social.app.db.entity.conversation.ConversationEntity;
import com.example.social.app.db.entity.conversation.ConversationParticipantEntity;
import com.example.social.app.db.entity.message.MessageEntity;
import com.example.social.app.db.entity.messagerequest.MessageRequestEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import com.example.social.app.enums.MessageRequestStatus;

import java.time.LocalDateTime;
import java.util.List;

public class MessageServiceImplDataset {

    public static UsersEntity currentUser() {
        UsersEntity u = new UsersEntity();
        u.setId(1L);
        u.setKeycloakId("kc-current");
        u.setFirstName("Current");
        u.setLastName("User");
        u.setEmail("current@example.com");
        u.setRole("USER");
        return u;
    }

    public static UsersEntity recipient() {
        UsersEntity u = new UsersEntity();
        u.setId(2L);
        u.setKeycloakId("kc-recipient");
        u.setFirstName("Recipient");
        u.setLastName("User");
        u.setEmail("recipient@example.com");
        u.setRole("USER");
        return u;
    }

    public static ConversationEntity existingConversation() {
        ConversationEntity c = new ConversationEntity();
        c.setId(100L);
        c.setUpdatedAt(LocalDateTime.now().minusMinutes(5));
        return c;
    }

    public static ConversationParticipantEntity currentParticipant() {
        ConversationParticipantEntity cp = new ConversationParticipantEntity();
        cp.setId(1000L);
        cp.setConversation(existingConversation());
        cp.setUser(currentUser());
        return cp;
    }

    public static ConversationParticipantEntity recipientParticipant() {
        ConversationParticipantEntity cp = new ConversationParticipantEntity();
        cp.setId(1001L);
        cp.setConversation(existingConversation());
        cp.setUser(recipient());
        return cp;
    }

    public static List<ConversationParticipantEntity> bothParticipants() {
        return List.of(currentParticipant(), recipientParticipant());
    }

    public static MessageEntity sentMessage() {
        MessageEntity m = new MessageEntity();
        m.setId(200L);
        m.setConversation(existingConversation());
        m.setSender(currentUser());
        m.setContent("Hello!");
        m.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        return m;
    }

    public static MessageEntity receivedMessage() {
        MessageEntity m = new MessageEntity();
        m.setId(201L);
        m.setConversation(existingConversation());
        m.setSender(recipient());
        m.setContent("Hi there!");
        m.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        return m;
    }

    public static MessageRequestEntity pendingRequest() {
        MessageRequestEntity r = new MessageRequestEntity();
        r.setId(300L);
        r.setFromUser(currentUser());
        r.setToUser(recipient());
        r.setStatus(MessageRequestStatus.PENDING);
        r.setCreatedAt(LocalDateTime.now().minusHours(1));
        return r;
    }

    public static UserBriefDTO currentUserBrief() {
        UserBriefDTO d = new UserBriefDTO();
        d.setId(1L);
        d.setFirstName("Current");
        d.setLastName("User");
        return d;
    }

    public static UserBriefDTO recipientBrief() {
        UserBriefDTO d = new UserBriefDTO();
        d.setId(2L);
        d.setFirstName("Recipient");
        d.setLastName("User");
        return d;
    }

    public static MessageDTO messageDTO() {
        MessageDTO d = new MessageDTO();
        d.setId(200L);
        d.setConversationId(100L);
        d.setSender(currentUserBrief());
        d.setContent("Hello!");
        d.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        d.setRead(false);
        d.setEditable(true);
        return d;
    }
}
