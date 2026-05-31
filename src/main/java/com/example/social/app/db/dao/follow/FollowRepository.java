package com.example.social.app.db.dao.follow;

import com.example.social.app.db.entity.follow.FollowEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<FollowEntity, Long> {
    Optional<FollowEntity> findByFollowerAndFollowing(UsersEntity follower, UsersEntity following);

    boolean existsByFollowerAndFollowing(UsersEntity follower, UsersEntity following);

    long countByFollower(UsersEntity follower);

    long countByFollowing(UsersEntity following);

    Page<FollowEntity> findByFollower(UsersEntity follower, Pageable pageable);

    Page<FollowEntity> findByFollowing(UsersEntity following, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT f.following.id FROM FollowEntity f WHERE f.follower = :follower")
    List<Long> findFollowingUserIdsByFollower(@org.springframework.data.repository.query.Param("follower") UsersEntity follower);

    @org.springframework.data.jpa.repository.Query("SELECT f1.following FROM FollowEntity f1 WHERE f1.follower = :user AND f1.following IN (SELECT f2.follower FROM FollowEntity f2 WHERE f2.following = :user)")
    List<UsersEntity> findMutualFollows(@org.springframework.data.repository.query.Param("user") UsersEntity user);
}
