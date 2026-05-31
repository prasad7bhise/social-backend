package com.example.social.app.business.service.keycloak.impl;

import com.example.social.app.business.dto.auth.ProfileUpdateRequest;
import com.example.social.app.business.service.keycloak.KeycloakAdminService;
import com.example.social.app.config.keycloak.KeycloakAdminProperties;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@AllArgsConstructor
public class KeycloakAdminServiceImpl implements KeycloakAdminService {

    private final KeycloakAdminProperties properties;
    private final RestClient restClient;

    @Override
    public void updateUser(String keycloakId, ProfileUpdateRequest request) {
        String token = getAdminToken();
        String url = "%s/admin/realms/%s/users/%s".formatted(
                properties.getServerUrl(), properties.getRealm(), keycloakId);

        Map<String, Object> body = new java.util.LinkedHashMap<>();
        if (request.getFirstName() != null) body.put("firstName", request.getFirstName());
        if (request.getLastName() != null) body.put("lastName", request.getLastName());
        if (request.getEmail() != null) body.put("email", request.getEmail());

        if (body.isEmpty()) return;

        restClient.put()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    private String getAdminToken() {
        String tokenUrl = "%s/realms/%s/protocol/openid-connect/token".formatted(
                properties.getServerUrl(), properties.getRealm());

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());

        Map<String, Object> response = restClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        return (String) response.get("access_token");
    }
}
