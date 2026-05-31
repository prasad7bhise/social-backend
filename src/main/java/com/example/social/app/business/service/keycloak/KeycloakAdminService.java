package com.example.social.app.business.service.keycloak;

import com.example.social.app.business.dto.auth.ProfileUpdateRequest;

public interface KeycloakAdminService {
    void updateUser(String keycloakId, ProfileUpdateRequest request);
}
