package com.example.social.app.business.dto.message;

import com.example.social.app.business.dto.feed.UserBriefDTO;
import com.example.social.app.business.dto.messagereaction.MessageReactionDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long conversationId;
    private UserBriefDTO sender;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private boolean read;
    private boolean editable;
    private List<MessageReactionDTO> reactions;
}
