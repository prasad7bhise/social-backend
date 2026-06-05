package com.example.social.business.service.auth.dataset;

import com.example.social.app.business.dto.auth.CreateUserDTO;
import com.example.social.app.business.dto.auth.LogUserDTO;
import com.example.social.app.db.entity.user.UsersEntity;

public class AuthServiceImplDataset {

    public static CreateUserDTO validCreateUserDTO() {
        CreateUserDTO d = new CreateUserDTO();
        d.setEmail("newuser@example.com");
        d.setPassword("password123");
        d.setFirstName("New");
        d.setLastName("User");
        return d;
    }

    public static CreateUserDTO duplicateCreateUserDTO() {
        CreateUserDTO d = new CreateUserDTO();
        d.setEmail("existing@example.com");
        d.setPassword("password123");
        d.setFirstName("Existing");
        d.setLastName("User");
        return d;
    }

    public static LogUserDTO validLoginDTO() {
        LogUserDTO d = new LogUserDTO();
        d.setEmail("existing@example.com");
        d.setPassword("password123");
        return d;
    }

    public static UsersEntity existingUser() {
        UsersEntity u = new UsersEntity();
        u.setId(1L);
        u.setEmail("existing@example.com");
        u.setFirstName("Existing");
        u.setLastName("User");
        u.setRole("User");
        return u;
    }

    public static UsersEntity createdUser() {
        UsersEntity u = new UsersEntity();
        u.setEmail("newuser@example.com");
        u.setFirstName("New");
        u.setLastName("User");
        u.setRole("USER");
        u.setKeycloakId("keycloak-uuid");
        return u;
    }
}
