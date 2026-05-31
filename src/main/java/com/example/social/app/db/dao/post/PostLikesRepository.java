package com.example.social.app.db.dao.post;

import com.example.social.app.db.entity.post.PostEntity;
import com.example.social.app.db.entity.post.PostLikesEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikesRepository extends JpaRepository<PostLikesEntity, Long> {
    Optional<PostLikesEntity> findByPostAndUser(PostEntity post, UsersEntity user);

    long countByPost(PostEntity post);

    boolean existsByPostAndUser(PostEntity post, UsersEntity user);
}
