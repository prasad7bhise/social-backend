package com.example.social.app.controller;

import com.example.social.app.business.dto.feed.*;
import com.example.social.app.business.service.feed.FeedService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/feed")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<PostDTO>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(feedService.getFeed(page, size, jwt.getSubject()));
    }

    @GetMapping("/explore")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<PostDTO>> getExplore(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(feedService.getExplore(page, size, jwt.getSubject()));
    }

    @GetMapping("/posts/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostDTO> getPost(@PathVariable Long id,
                                           @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(feedService.getPost(id, jwt.getSubject()));
    }

    @PostMapping("/posts")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PostDTO> createPost(@RequestBody CreatePostRequest request,
                                              @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedService.createPost(request, jwt.getSubject()));
    }

    @DeleteMapping("/posts/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deletePost(@PathVariable Long id,
                                           @AuthenticationPrincipal Jwt jwt) {
        feedService.deletePost(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{id}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> toggleLike(@PathVariable Long id,
                                           @AuthenticationPrincipal Jwt jwt) {
        feedService.toggleLike(id, jwt.getSubject());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{id}/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long id,
                                                 @RequestBody CreateCommentRequest request,
                                                 @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedService.addComment(id, request, jwt.getSubject()));
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id,
                                              @AuthenticationPrincipal Jwt jwt) {
        feedService.deleteComment(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/comments/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long id,
                                                     @RequestBody CreateCommentRequest request,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(feedService.updateComment(id, request.getContent(), jwt.getSubject()));
    }
}
