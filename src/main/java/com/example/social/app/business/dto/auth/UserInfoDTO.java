package com.example.social.app.business.dto.auth;

import lombok.Data;

@Data
public class UserInfoDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
}
