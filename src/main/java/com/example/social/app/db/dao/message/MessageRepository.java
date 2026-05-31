package com.example.social.app.db.dao.message;

import com.example.social.app.db.entity.message.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    Page<MessageEntity> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    Optional<MessageEntity> findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);

    long countByConversationIdAndReadAtIsNullAndSenderIdNot(Long conversationId, Long senderId);

    @Modifying
    @Query("UPDATE MessageEntity m SET m.readAt = :now WHERE m.conversation.id = :conversationId AND m.sender.id <> :userId AND m.readAt IS NULL")
    void markAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("now") LocalDateTime now);
}
