package com.example.social.business.service.auth;

import com.example.social.app.business.dto.auth.CreateUserDTO;
import com.example.social.app.business.dto.auth.LogUserDTO;
import com.example.social.app.business.mapper.UserMapper;
import com.example.social.app.business.record.auth.AuthResponse;
import com.example.social.app.business.service.auth.impl.AuthServiceImpl;
import com.example.social.app.business.service.keycloak.KeycloakAdminService;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.user.UsersEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.example.social.business.service.auth.dataset.AuthServiceImplDataset.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplUnitTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void test01_registerUser_shouldRegisterNewUser() {
        CreateUserDTO dto = validCreateUserDTO();
        when(usersRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(keycloakAdminService.createUser(dto)).thenReturn("keycloak-uuid");
        when(userMapper.mapDTOToEntity("keycloak-uuid", "newuser@example.com", "New", "User", "USER"))
                .thenReturn(createdUser());

        AuthResponse response = authService.registerUser(dto);

        assertThat(response.message()).isEqualTo("User registered successfully");
        verify(keycloakAdminService).createUser(dto);
        verify(userMapper).mapDTOToEntity("keycloak-uuid", "newuser@example.com", "New", "User", "USER");
        verify(usersRepository).save(createdUser());
    }

    @Test
    void test02_registerUser_shouldThrowException_whenEmailExists() {
        CreateUserDTO dto = duplicateCreateUserDTO();
        when(usersRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser()));

        assertThatThrownBy(() -> authService.registerUser(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User already exists");
        verify(usersRepository, never()).save(any());
        verifyNoInteractions(keycloakAdminService);
    }

    @Test
    void test03_login_shouldReturnSuccess() {
        LogUserDTO dto = validLoginDTO();

        AuthResponse response = authService.login(dto);

        assertThat(response.message()).isEqualTo("Login Successful");
    }
}
