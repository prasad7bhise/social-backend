package com.example.social.app.business.dto.messagereaction;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageReactionDTO {
    private Long id;
    private Long userId;
    private String emoji;
    private String userFirstName;
}
