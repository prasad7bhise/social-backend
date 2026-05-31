package com.example.social.app.db.dao.conversation;

import com.example.social.app.db.entity.conversation.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<ConversationEntity, Long> {
}
