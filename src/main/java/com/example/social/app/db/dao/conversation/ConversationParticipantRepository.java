package com.example.social.app.db.dao.conversation;

import com.example.social.app.db.entity.conversation.ConversationParticipantEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipantEntity, Long> {

    @Query("SELECT cp FROM ConversationParticipantEntity cp JOIN FETCH cp.conversation c " +
           "WHERE cp.user = :user ORDER BY c.updatedAt DESC")
    List<ConversationParticipantEntity> findByUserWithConversation(@Param("user") UsersEntity user);

    @Query("SELECT cp.conversation.id FROM ConversationParticipantEntity cp WHERE cp.user.id = :userId1 " +
           "AND cp.conversation.id IN (SELECT cp2.conversation.id FROM ConversationParticipantEntity cp2 WHERE cp2.user.id = :userId2)")
    List<Long> findCommonConversationIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    Optional<ConversationParticipantEntity> findByConversationIdAndUserId(Long conversationId, Long userId);

    List<ConversationParticipantEntity> findByConversationId(Long conversationId);
}
