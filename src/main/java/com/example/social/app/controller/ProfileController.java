package com.example.social.app.controller;

import com.example.social.app.business.dto.auth.ProfileUpdateRequest;
import com.example.social.app.business.dto.auth.UserInfoDTO;
import com.example.social.app.business.service.profile.ProfileService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@AllArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserInfoDTO> getProfile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(profileService.getProfile(jwt.getSubject()));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserInfoDTO> getUserProfile(@PathVariable Integer userId) {
        return ResponseEntity.ok(profileService.getProfileByDbId(userId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserInfoDTO>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(profileService.searchUsers(q));
    }

    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserInfoDTO> updateProfile(@AuthenticationPrincipal Jwt jwt,
                                                     @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(jwt.getSubject(), request));
    }
}
