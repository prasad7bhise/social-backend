package com.example.social.app.business.dto.auth;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String bio;
    private String avatarUrl;
}
