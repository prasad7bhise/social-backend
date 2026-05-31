package com.example.social.business.service.sync.dataset;

import com.example.social.app.business.dto.auth.UserInfoDTO;
import com.example.social.app.db.entity.user.UsersEntity;

public class UserSyncServiceImplDataset {

    public static UsersEntity existingUser() {
        UsersEntity u = new UsersEntity();
        u.setId(1L);
        u.setKeycloakId("kc-user-123");
        u.setEmail("old@example.com");
        u.setFirstName("Old");
        u.setLastName("Name");
        u.setRole("USER");
        return u;
    }

    public static UsersEntity updatedUser() {
        UsersEntity u = new UsersEntity();
        u.setId(1L);
        u.setKeycloakId("kc-user-123");
        u.setEmail("new@example.com");
        u.setFirstName("New");
        u.setLastName("Name");
        u.setRole("USER");
        return u;
    }

    public static UserInfoDTO existingUserDTO() {
        UserInfoDTO d = new UserInfoDTO();
        d.setId(1L);
        d.setFirstName("Old");
        d.setLastName("Name");
        d.setEmail("old@example.com");
        d.setRole("USER");
        return d;
    }

    public static UserInfoDTO updatedUserDTO() {
        UserInfoDTO d = new UserInfoDTO();
        d.setId(1L);
        d.setFirstName("New");
        d.setLastName("Name");
        d.setEmail("new@example.com");
        d.setRole("USER");
        return d;
    }
}
