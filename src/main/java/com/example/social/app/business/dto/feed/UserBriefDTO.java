package com.example.social.app.business.dto.feed;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBriefDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String avatarUrl;
}
