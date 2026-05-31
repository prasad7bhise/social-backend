package com.example.social.app.business.dto.message;

import com.example.social.app.business.dto.feed.UserBriefDTO;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private Long id;
    private UserBriefDTO participant;
    private MessageDTO lastMessage;
    private long unreadCount;
    private LocalDateTime updatedAt;
}
