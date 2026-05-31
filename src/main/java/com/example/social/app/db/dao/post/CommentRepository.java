package com.example.social.app.db.dao.post;

import com.example.social.app.db.entity.post.CommentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentsEntity, Long> {
    List<CommentsEntity> findByPostIdOrderByCreatedAtDesc(Long postId);
}
