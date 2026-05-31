package com.example.social.app.business.service.messagereaction;

import com.example.social.app.business.dto.messagereaction.MessageReactionDTO;

import java.util.List;

public interface MessageReactionService {

    List<MessageReactionDTO> getReactions(Long messageId);

    MessageReactionDTO addReaction(String keycloakId, Long messageId, String emoji);

    void removeReaction(String keycloakId, Long messageId, String emoji);
}
