package com.example.social.app.business.service.auth.impl;

import com.example.social.app.business.dto.auth.CreateUserDTO;
import com.example.social.app.business.dto.auth.LogUserDTO;
import com.example.social.app.business.mapper.UserMapper;
import com.example.social.app.business.record.auth.AuthResponse;
import com.example.social.app.business.service.auth.AuthService;
import com.example.social.app.business.service.keycloak.KeycloakAdminService;
import com.example.social.app.db.dao.users.UsersRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsersRepository usersRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final UserMapper userMapper;

    @Override
    public AuthResponse registerUser(CreateUserDTO createUserDTO) {
        if (usersRepository.findByEmail(createUserDTO.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        String keycloakId = keycloakAdminService.createUser(createUserDTO);

        usersRepository.save(userMapper.mapDTOToEntity(
                keycloakId,
                createUserDTO.getEmail(),
                createUserDTO.getFirstName(),
                createUserDTO.getLastName(),
                "USER"
        ));

        return new AuthResponse("User registered successfully");
    }

    public AuthResponse login(LogUserDTO logUserDTO) {
        return new AuthResponse("Login Successful");
    }
}
