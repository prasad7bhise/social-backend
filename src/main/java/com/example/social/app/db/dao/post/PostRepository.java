package com.example.social.app.db.dao.post;

import com.example.social.app.db.entity.post.PostEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    Page<PostEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<PostEntity> findByUserOrderByCreatedAtDesc(UsersEntity user, Pageable pageable);

    Page<PostEntity> findByUserIdInOrderByCreatedAtDesc(java.util.List<Long> userIds, Pageable pageable);

    long countByUserId(Long userId);
}
