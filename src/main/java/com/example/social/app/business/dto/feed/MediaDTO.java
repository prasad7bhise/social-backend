package com.example.social.app.business.dto.feed;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaDTO {
    private Long id;
    private String type;
    private String url;
}
