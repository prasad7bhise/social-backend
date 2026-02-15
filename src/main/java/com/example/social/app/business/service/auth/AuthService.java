package com.example.social.app.business.service.auth;

import com.example.social.app.business.dto.auth.CreateUserDTO;
import com.example.social.app.business.dto.auth.LogUserDTO;
import com.example.social.app.business.record.auth.AuthResponse;

public interface AuthService {
    AuthResponse registerUser(CreateUserDTO createUserDTO);

    AuthResponse login(LogUserDTO logUserDTO);
}
