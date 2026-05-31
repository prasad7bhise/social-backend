package com.example.social.app.db.dao.messagereaction;

import com.example.social.app.db.entity.messagereaction.MessageReactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageReactionRepository extends JpaRepository<MessageReactionEntity, Long> {

    List<MessageReactionEntity> findByMessageId(Long messageId);

    Optional<MessageReactionEntity> findByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);

    boolean existsByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);

    void deleteByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);
}
