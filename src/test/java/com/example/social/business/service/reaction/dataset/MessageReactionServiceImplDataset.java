package com.example.social.business.service.reaction.dataset;

import com.example.social.app.business.dto.messagereaction.MessageReactionDTO;
import com.example.social.app.db.entity.message.MessageEntity;
import com.example.social.app.db.entity.messagereaction.MessageReactionEntity;
import com.example.social.app.db.entity.user.UsersEntity;

public class MessageReactionServiceImplDataset {

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

    public static MessageEntity existingMessage() {
        MessageEntity m = new MessageEntity();
        m.setId(50L);
        m.setContent("Hello");
        return m;
    }

    public static MessageReactionEntity existingReaction() {
        MessageReactionEntity r = new MessageReactionEntity();
        r.setId(200L);
        r.setMessage(existingMessage());
        r.setUser(currentUser());
        r.setEmoji("👍");
        return r;
    }

    public static MessageReactionDTO reactionDTO() {
        return new MessageReactionDTO(200L, 1L, "👍", "Current");
    }

    public static MessageReactionEntity heartReaction() {
        MessageReactionEntity r = new MessageReactionEntity();
        r.setId(201L);
        r.setMessage(existingMessage());
        r.setUser(currentUser());
        r.setEmoji("❤️");
        return r;
    }

    public static MessageReactionDTO heartReactionDTO() {
        return new MessageReactionDTO(201L, 1L, "❤️", "Current");
    }
}
