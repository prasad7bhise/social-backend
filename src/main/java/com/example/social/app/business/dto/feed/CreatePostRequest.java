package com.example.social.app.business.dto.feed;

import lombok.Data;

import java.util.List;

@Data
public class CreatePostRequest {
    private String content;
    private List<MediaRequest> media;
}
