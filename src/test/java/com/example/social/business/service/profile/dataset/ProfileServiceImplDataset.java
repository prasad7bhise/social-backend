package com.example.social.business.service.profile.dataset;

import com.example.social.app.business.dto.auth.ProfileUpdateRequest;
import com.example.social.app.business.dto.auth.UserInfoDTO;
import com.example.social.app.db.entity.user.UsersEntity;

import java.util.List;

public class ProfileServiceImplDataset {

    public static UsersEntity existingUser() {
        UsersEntity u = new UsersEntity();
        u.setId(1L);
        u.setKeycloakId("kc-user-123");
        u.setFirstName("John");
        u.setLastName("Doe");
        u.setEmail("john@example.com");
        u.setRole("USER");
        u.setBio("Hello world");
        u.setAvatarUrl("/avatars/john.jpg");
        return u;
    }

    public static UserInfoDTO existingUserDTO() {
        UserInfoDTO d = new UserInfoDTO();
        d.setId(1L);
        d.setFirstName("John");
        d.setLastName("Doe");
        d.setEmail("john@example.com");
        d.setRole("USER");
        d.setBio("Hello world");
        d.setAvatarUrl("/avatars/john.jpg");
        d.setPostCount(5);
        return d;
    }

    public static ProfileUpdateRequest updateLastNameRequest() {
        ProfileUpdateRequest r = new ProfileUpdateRequest();
        r.setLastName("Smith");
        return r;
    }

    public static ProfileUpdateRequest updateAllFieldsRequest() {
        ProfileUpdateRequest r = new ProfileUpdateRequest();
        r.setFirstName("Jane");
        r.setLastName("Smith");
        r.setEmail("jane.smith@example.com");
        r.setBio("New bio");
        r.setAvatarUrl("/avatars/jane.jpg");
        return r;
    }

    public static ProfileUpdateRequest emptyRequest() {
        return new ProfileUpdateRequest();
    }

    public static UsersEntity searchedUser() {
        UsersEntity u = new UsersEntity();
        u.setId(2L);
        u.setKeycloakId("kc-user-456");
        u.setFirstName("Alice");
        u.setLastName("Brown");
        u.setEmail("alice@example.com");
        u.setRole("USER");
        return u;
    }

    public static List<UsersEntity> searchResults() {
        return List.of(searchedUser());
    }

    public static UserInfoDTO searchedUserDTO() {
        UserInfoDTO d = new UserInfoDTO();
        d.setId(2L);
        d.setFirstName("Alice");
        d.setLastName("Brown");
        d.setEmail("alice@example.com");
        d.setRole("USER");
        return d;
    }
}
