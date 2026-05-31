package com.example.social.app.business.dto.message;

import com.example.social.app.business.dto.feed.UserBriefDTO;
import com.example.social.app.enums.MessageRequestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequestDTO {
    private Long id;
    private UserBriefDTO fromUser;
    private UserBriefDTO toUser;
    private MessageRequestStatus status;
    private LocalDateTime createdAt;
}
