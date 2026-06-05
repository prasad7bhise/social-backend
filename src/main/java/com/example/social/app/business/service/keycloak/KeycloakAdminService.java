package com.example.social.app.business.service.keycloak;

import com.example.social.app.business.dto.auth.CreateUserDTO;
import com.example.social.app.business.dto.auth.ProfileUpdateRequest;

public interface KeycloakAdminService {
    String createUser(CreateUserDTO dto);
    void updateUser(String keycloakId, ProfileUpdateRequest request);
}
