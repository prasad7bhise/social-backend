package com.example.social.app.db.dao.messagerequest;

import com.example.social.app.db.entity.messagerequest.MessageRequestEntity;
import com.example.social.app.enums.MessageRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRequestRepository extends JpaRepository<MessageRequestEntity, Long> {

    List<MessageRequestEntity> findByToUserIdAndStatusOrderByCreatedAtDesc(Long toUserId, MessageRequestStatus status);

    List<MessageRequestEntity> findByFromUserIdAndStatusOrderByCreatedAtDesc(Long fromUserId, MessageRequestStatus status);

    Optional<MessageRequestEntity> findByFromUserIdAndToUserIdAndStatus(Long fromUserId, Long toUserId, MessageRequestStatus status);
}
