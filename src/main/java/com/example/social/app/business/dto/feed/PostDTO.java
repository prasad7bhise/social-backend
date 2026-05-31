package com.example.social.app.business.dto.feed;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private String content;
    private UserBriefDTO user;
    private List<MediaDTO> media;
    private long likeCount;
    private long commentCount;
    private boolean likedByMe;
    private List<CommentDTO> recentComments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
