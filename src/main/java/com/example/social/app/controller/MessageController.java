package com.example.social.app.controller;

import com.example.social.app.business.dto.message.ConversationDTO;
import com.example.social.app.business.dto.message.MessageDTO;
import com.example.social.app.business.dto.message.MessageRequestDTO;
import com.example.social.app.business.dto.message.SendMessageRequest;
import com.example.social.app.business.service.message.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/conversations/{recipientId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> createOrGetConversation(
            @PathVariable Long recipientId,
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> result = messageService.createOrGetConversation(
                jwt.getSubject(), recipientId, request.getContent());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ConversationDTO>> getConversations(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(messageService.getConversations(jwt.getSubject()));
    }

    @GetMapping("/conversations/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ConversationDTO> getConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(messageService.getConversation(jwt.getSubject(), id));
    }

    @PatchMapping("/messages/{messageId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageDTO> updateMessage(
            @PathVariable Long messageId,
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(messageService.updateMessage(jwt.getSubject(), messageId, request.getContent()));
    }

    @PatchMapping("/conversations/{id}/read")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        messageService.markMessagesAsRead(jwt.getSubject(), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/conversations/{id}/messages")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<MessageDTO>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(messageService.getMessages(jwt.getSubject(), id, page, size));
    }

    @PostMapping("/conversations/{id}/messages")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageDTO> sendMessage(
            @PathVariable Long id,
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.sendMessage(jwt.getSubject(), id, request.getContent()));
    }

    @GetMapping("/message-requests")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<MessageRequestDTO>> getMessageRequests(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(messageService.getMessageRequests(jwt.getSubject()));
    }

    @PostMapping("/message-requests/{id}/accept")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ConversationDTO> acceptMessageRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(messageService.acceptMessageRequest(jwt.getSubject(), id));
    }

    @PostMapping("/message-requests/{id}/decline")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> declineMessageRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        messageService.declineMessageRequest(jwt.getSubject(), id);
        return ResponseEntity.noContent().build();
    }
}
