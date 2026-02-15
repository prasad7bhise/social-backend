package com.example.social.app.controller;

import com.example.social.app.business.dto.auth.CreateUserDTO;
import com.example.social.app.business.dto.auth.LogUserDTO;
import com.example.social.app.business.record.auth.AuthResponse;
import com.example.social.app.business.service.auth.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse registerUser(@RequestBody CreateUserDTO createUserDTO) {
        return authService.registerUser(createUserDTO);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LogUserDTO logUserDTO) {
        return authService.login(logUserDTO);
    }
}
