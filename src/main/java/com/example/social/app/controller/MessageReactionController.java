package com.example.social.app.controller;

import com.example.social.app.business.dto.messagereaction.MessageReactionDTO;
import com.example.social.app.business.service.messagereaction.MessageReactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages/{messageId}/reactions")
@AllArgsConstructor
public class MessageReactionController {

    private final MessageReactionService reactionService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<MessageReactionDTO>> getReactions(@PathVariable Long messageId) {
        return ResponseEntity.ok(reactionService.getReactions(messageId));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageReactionDTO> addReaction(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(reactionService.addReaction(jwt.getSubject(), messageId, body.get("emoji")));
    }

    @DeleteMapping("/{emoji}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeReaction(
            @PathVariable Long messageId,
            @PathVariable String emoji,
            @AuthenticationPrincipal Jwt jwt) {
        reactionService.removeReaction(jwt.getSubject(), messageId, emoji);
        return ResponseEntity.noContent().build();
    }
}
