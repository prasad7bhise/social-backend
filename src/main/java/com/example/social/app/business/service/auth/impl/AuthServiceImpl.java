package com.example.social.app.business.service.auth.impl;

import com.example.social.app.business.dto.auth.CreateUserDTO;
import com.example.social.app.business.dto.auth.LogUserDTO;
import com.example.social.app.business.record.auth.AuthResponse;
import com.example.social.app.business.service.auth.AuthService;
import com.example.social.app.config.security.JwtServiceConfig;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.user.UsersEntity;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtServiceConfig jwtServiceConfig;

    @Override
    public AuthResponse registerUser(CreateUserDTO createUserDTO) {
        if (usersRepository.findByEmail(createUserDTO.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        final UsersEntity user = new UsersEntity();
        user.setEmail(createUserDTO.getEmail());
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));
        user.setRole("User");
        user.setFirstName(createUserDTO.getFirstName());
        user.setLastName(createUserDTO.getLastName());
        usersRepository.save(user);

        return new AuthResponse("User registered successfully");
    }

    public AuthResponse login(LogUserDTO logUserDTO) {

        final UsersEntity user = usersRepository.findByEmail(logUserDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(logUserDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtServiceConfig.generateToken(user.getEmail());
        return new AuthResponse(token);
    }
}
