package com.example.social.business.service.sync;

import com.example.social.app.business.dto.auth.UserInfoDTO;
import com.example.social.app.business.mapper.UserMapper;
import com.example.social.app.business.service.sync.impl.UserSyncServiceImpl;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.user.UsersEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static com.example.social.business.service.sync.dataset.UserSyncServiceImplDataset.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSyncServiceImplUnitTest {

    @Mock
    private UsersRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserSyncServiceImpl userSyncService;

    @Test
    void test01_syncUser_shouldCreateNewUser_whenNotExists() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("kc-new");
        when(jwt.getClaim("email")).thenReturn("new@example.com");
        when(jwt.getClaim("given_name")).thenReturn("New");
        when(jwt.getClaim("family_name")).thenReturn("User");

        when(userRepository.findByKeycloakId("kc-new")).thenReturn(Optional.empty());

        UsersEntity savedUser = new UsersEntity();
        savedUser.setId(1L);
        savedUser.setKeycloakId("kc-new");
        savedUser.setEmail("new@example.com");
        savedUser.setFirstName("New");
        savedUser.setLastName("User");
        savedUser.setRole("USER");

        when(userMapper.mapDTOToEntity("kc-new", "new@example.com", "New", "User", "USER"))
                .thenReturn(savedUser);
        when(userRepository.save(savedUser)).thenReturn(savedUser);

        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(1L);
        dto.setFirstName("New");
        dto.setLastName("User");
        when(userMapper.mapEntityToDTO(savedUser)).thenReturn(dto);

        UserInfoDTO result = userSyncService.syncUser(jwt);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("New");
        verify(userRepository).save(savedUser);
    }

    @Test
    void test02_syncUser_shouldUpdateExistingUser_whenChanged() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("kc-user-123");
        when(jwt.getClaim("email")).thenReturn("new@example.com");
        when(jwt.getClaim("given_name")).thenReturn("New");
        when(jwt.getClaim("family_name")).thenReturn("Updated");

        UsersEntity existing = existingUser();
        when(userRepository.findByKeycloakId("kc-user-123")).thenReturn(Optional.of(existing));

        UsersEntity updated = updatedUser();
        updated.setLastName("Updated");
        when(userRepository.save(existing)).thenReturn(updated);

        UserInfoDTO dto = updatedUserDTO();
        dto.setLastName("Updated");
        when(userMapper.mapEntityToDTO(updated)).thenReturn(dto);

        UserInfoDTO result = userSyncService.syncUser(jwt);

        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getLastName()).isEqualTo("Updated");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userRepository).save(existing);
    }

    @Test
    void test03_syncUser_shouldNotSave_whenNoChanges() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("kc-user-123");
        when(jwt.getClaim("email")).thenReturn("old@example.com");
        when(jwt.getClaim("given_name")).thenReturn("Old");
        when(jwt.getClaim("family_name")).thenReturn("Name");

        UsersEntity existing = existingUser();
        when(userRepository.findByKeycloakId("kc-user-123")).thenReturn(Optional.of(existing));
        when(userMapper.mapEntityToDTO(existing)).thenReturn(existingUserDTO());

        UserInfoDTO result = userSyncService.syncUser(jwt);

        assertThat(result.getEmail()).isEqualTo("old@example.com");
        verify(userRepository, never()).save(any());
    }
}
