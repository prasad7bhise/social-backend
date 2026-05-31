package com.example.social.app.controller;

import com.example.social.app.business.dto.auth.UserInfoDTO;
import com.example.social.app.business.service.follow.FollowService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/follow/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> follow(@PathVariable Long userId,
                                       @AuthenticationPrincipal Jwt jwt) {
        followService.follow(jwt.getSubject(), userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/follow/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> unfollow(@PathVariable Long userId,
                                         @AuthenticationPrincipal Jwt jwt) {
        followService.unfollow(jwt.getSubject(), userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/follow/{userId}/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Boolean>> isFollowing(@PathVariable Long userId,
                                                            @AuthenticationPrincipal Jwt jwt) {
        boolean following = followService.isFollowing(jwt.getSubject(), userId);
        return ResponseEntity.ok(Map.of("following", following));
    }

    @GetMapping("/follow/{userId}/counts")
    public ResponseEntity<Map<String, Long>> getCounts(@PathVariable Long userId) {
        long followers = followService.getFollowerCount(userId);
        long following = followService.getFollowingCount(userId);
        return ResponseEntity.ok(Map.of("followers", followers, "following", following));
    }

    @GetMapping("/me/followers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<UserInfoDTO>> getFollowers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(followService.getFollowers(jwt.getSubject(), page, size));
    }

    @GetMapping("/follow/suggestions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserInfoDTO>> getMessageSuggestions(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(followService.getMessageSuggestions(jwt.getSubject()));
    }

    @GetMapping("/me/following")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<UserInfoDTO>> getFollowing(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(followService.getFollowing(jwt.getSubject(), page, size));
    }
}
