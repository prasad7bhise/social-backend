package com.example.social.business.service.profile;

import com.example.social.app.business.dto.auth.ProfileUpdateRequest;
import com.example.social.app.business.dto.auth.UserInfoDTO;
import com.example.social.app.business.mapper.UserMapper;
import com.example.social.app.business.service.keycloak.KeycloakAdminService;
import com.example.social.app.business.service.profile.impl.ProfileServiceImpl;
import com.example.social.app.db.dao.post.PostRepository;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.user.UsersEntity;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.example.social.business.service.profile.dataset.ProfileServiceImplDataset.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplUnitTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private UsersEntity user;
    private UserInfoDTO userDTO;

    @BeforeEach
    void setUp() {
        user = existingUser();
        userDTO = existingUserDTO();
    }

    @Test
    void test01_getProfile_shouldReturnUserInfoDTO() {
        when(usersRepository.findByKeycloakId("kc-user-123")).thenReturn(Optional.of(user));
        when(postRepository.countByUserId(1L)).thenReturn(5L);
        when(userMapper.mapEntityToDTO(user, 5L)).thenReturn(userDTO);

        UserInfoDTO result = profileService.getProfile("kc-user-123");

        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getPostCount()).isEqualTo(5);
    }

    @Test
    void test02_getProfile_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(usersRepository.findByKeycloakId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile("unknown"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void test03_getProfileByDbId_shouldReturnUserInfoDTO() {
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.countByUserId(1L)).thenReturn(3L);
        when(userMapper.mapEntityToDTO(user, 3L)).thenReturn(userDTO);

        UserInfoDTO result = profileService.getProfileByDbId(1);

        assertThat(result).isNotNull();
    }

    @Test
    void test04_getProfileByDbId_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(usersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfileByDbId(99))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void test05_updateProfile_shouldUpdateLastNameOnly() {
        ProfileUpdateRequest request = updateLastNameRequest();
        when(usersRepository.findByKeycloakId("kc-user-123")).thenReturn(Optional.of(user));
        when(usersRepository.save(any())).thenReturn(user);
        when(userMapper.mapEntityToDTO(any())).thenReturn(userDTO);

        UserInfoDTO result = profileService.updateProfile("kc-user-123", request);

        verify(keycloakAdminService).updateUser("kc-user-123", request);
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getFirstName()).isEqualTo("John");
        verify(usersRepository).save(user);
    }

    @Test
    void test06_updateProfile_shouldUpdateAllFields() {
        ProfileUpdateRequest request = updateAllFieldsRequest();
        when(usersRepository.findByKeycloakId("kc-user-123")).thenReturn(Optional.of(user));
        when(usersRepository.save(any())).thenReturn(user);
        when(userMapper.mapEntityToDTO(any())).thenReturn(userDTO);

        profileService.updateProfile("kc-user-123", request);

        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(user.getBio()).isEqualTo("New bio");
        assertThat(user.getAvatarUrl()).isEqualTo("/avatars/jane.jpg");
    }

    @Test
    void test07_updateProfile_shouldSkipNullFields() {
        ProfileUpdateRequest request = emptyRequest();
        when(usersRepository.findByKeycloakId("kc-user-123")).thenReturn(Optional.of(user));
        when(usersRepository.save(any())).thenReturn(user);
        when(userMapper.mapEntityToDTO(any())).thenReturn(userDTO);

        profileService.updateProfile("kc-user-123", request);

        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void test08_updateProfile_shouldSucceedWithAsyncKeycloakSync() {
        ProfileUpdateRequest request = updateLastNameRequest();
        when(usersRepository.findByKeycloakId("kc-user-123")).thenReturn(Optional.of(user));
        when(usersRepository.save(any())).thenReturn(user);
        when(userMapper.mapEntityToDTO(any())).thenReturn(userDTO);

        UserInfoDTO result = profileService.updateProfile("kc-user-123", request);

        verify(keycloakAdminService).updateUser("kc-user-123", request);
        assertThat(user.getLastName()).isEqualTo("Smith");
        verify(usersRepository).save(user);
    }

    @Test
    void test09_updateProfile_shouldThrowEntityNotFoundException_whenUserNotFound() {
        ProfileUpdateRequest request = updateLastNameRequest();
        when(usersRepository.findByKeycloakId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.updateProfile("unknown", request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void test10_searchUsers_shouldReturnMatchingUsers() {
        List<UsersEntity> results = searchResults();
        when(usersRepository.searchByName("Ali")).thenReturn(results);
        when(userMapper.mapEntityToDTO(searchedUser())).thenReturn(searchedUserDTO());

        List<UserInfoDTO> result = profileService.searchUsers("Ali");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Alice");
    }

    @Test
    void test11_searchUsers_shouldReturnEmptyList_whenNoMatches() {
        when(usersRepository.searchByName("Nonexistent")).thenReturn(List.of());

        List<UserInfoDTO> result = profileService.searchUsers("Nonexistent");

        assertThat(result).isEmpty();
    }
}
