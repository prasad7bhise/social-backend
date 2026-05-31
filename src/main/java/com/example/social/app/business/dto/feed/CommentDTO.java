package com.example.social.app.business.dto.feed;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private String content;
    private UserBriefDTO user;
    private LocalDateTime createdAt;
    private boolean editable;
}
