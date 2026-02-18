package com.example.social.app.controller;

import com.example.social.app.business.dto.auth.UserInfoDTO;
import com.example.social.app.business.service.sync.UserSyncService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@AllArgsConstructor
public class TestController {
    private final UserSyncService userSyncService;

    @GetMapping()
    @PreAuthorize("hasRole('USER')")
    public UserInfoDTO test(@AuthenticationPrincipal Jwt jwt) {
        return userSyncService.syncUser(jwt);
    }
}
