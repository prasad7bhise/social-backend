package com.example.social.business.service.follow.dataset;

import com.example.social.app.business.dto.auth.UserInfoDTO;
import com.example.social.app.db.entity.follow.FollowEntity;
import com.example.social.app.db.entity.user.UsersEntity;

import java.util.List;

public class FollowServiceImplDataset {

    public static UsersEntity currentUser() {
        UsersEntity u = new UsersEntity();
        u.setId(1L);
        u.setKeycloakId("kc-current");
        u.setFirstName("Current");
        u.setLastName("User");
        u.setEmail("current@example.com");
        u.setRole("USER");
        return u;
    }

    public static UsersEntity targetUser() {
        UsersEntity u = new UsersEntity();
        u.setId(2L);
        u.setKeycloakId("kc-target");
        u.setFirstName("Target");
        u.setLastName("User");
        u.setEmail("target@example.com");
        u.setRole("USER");
        return u;
    }

    public static FollowEntity followRelation() {
        FollowEntity f = new FollowEntity();
        f.setId(100L);
        f.setFollower(currentUser());
        f.setFollowing(targetUser());
        return f;
    }

    public static UserInfoDTO targetUserDTO() {
        UserInfoDTO d = new UserInfoDTO();
        d.setId(2L);
        d.setFirstName("Target");
        d.setLastName("User");
        d.setEmail("target@example.com");
        d.setRole("USER");
        return d;
    }

    public static List<UsersEntity> mutualFollows() {
        UsersEntity mutual = new UsersEntity();
        mutual.setId(3L);
        mutual.setKeycloakId("kc-mutual");
        mutual.setFirstName("Mutual");
        mutual.setLastName("Friend");
        mutual.setEmail("mutual@example.com");
        mutual.setRole("USER");
        return List.of(mutual);
    }
}
