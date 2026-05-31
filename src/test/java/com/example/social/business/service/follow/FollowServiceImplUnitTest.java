package com.example.social.business.service.follow;

import com.example.social.app.business.dto.auth.UserInfoDTO;
import com.example.social.app.business.mapper.UserMapper;
import com.example.social.app.business.service.follow.impl.FollowServiceImpl;
import com.example.social.app.business.service.notification.NotificationService;
import com.example.social.app.db.dao.follow.FollowRepository;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.user.UsersEntity;
import com.example.social.app.enums.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static com.example.social.business.service.follow.dataset.FollowServiceImplDataset.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplUnitTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FollowServiceImpl followService;

    private UsersEntity currentUser;
    private UsersEntity targetUser;

    @BeforeEach
    void setUp() {
        currentUser = currentUser();
        targetUser = targetUser();
    }

    @Test
    void test01_follow_shouldFollowAndNotify() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(usersRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRepository.existsByFollowerAndFollowing(currentUser, targetUser)).thenReturn(false);

        followService.follow("kc-current", 2L);

        verify(followRepository).save(any());
        verify(notificationService).createNotification(
                2L, 1L, NotificationType.FOLLOW, null, "Current started following you"
        );
    }

    @Test
    void test02_follow_shouldThrowException_whenFollowingSelf() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));

        assertThatThrownBy(() -> followService.follow("kc-current", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot follow yourself");
    }

    @Test
    void test03_follow_shouldThrowException_whenAlreadyFollowing() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(usersRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRepository.existsByFollowerAndFollowing(currentUser, targetUser)).thenReturn(true);

        assertThatThrownBy(() -> followService.follow("kc-current", 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Already following this user");
    }

    @Test
    void test04_unfollow_shouldUnfollow() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(usersRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRepository.findByFollowerAndFollowing(currentUser, targetUser))
                .thenReturn(Optional.of(followRelation()));

        followService.unfollow("kc-current", 2L);

        verify(followRepository).delete(followRelation());
    }

    @Test
    void test05_unfollow_shouldThrowException_whenNotFollowing() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(usersRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRepository.findByFollowerAndFollowing(currentUser, targetUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.unfollow("kc-current", 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Not following this user");
    }

    @Test
    void test06_isFollowing_shouldReturnTrue() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(usersRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRepository.existsByFollowerAndFollowing(currentUser, targetUser)).thenReturn(true);

        boolean result = followService.isFollowing("kc-current", 2L);

        assertThat(result).isTrue();
    }

    @Test
    void test07_getFollowers_shouldReturnFollowers() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(followRepository.findByFollowing(eq(currentUser), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(followRelation())));
        when(userMapper.mapEntityToDTO(currentUser)).thenReturn(new UserInfoDTO());

        Page<UserInfoDTO> result = followService.getFollowers("kc-current", 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void test08_getFollowing_shouldReturnFollowing() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(followRepository.findByFollower(eq(currentUser), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(followRelation())));
        when(userMapper.mapEntityToDTO(targetUser)).thenReturn(targetUserDTO());

        Page<UserInfoDTO> result = followService.getFollowing("kc-current", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Target");
    }

    @Test
    void test09_getFollowerCount_shouldReturnCount() {
        when(usersRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRepository.countByFollowing(targetUser)).thenReturn(5L);

        long count = followService.getFollowerCount(2L);

        assertThat(count).isEqualTo(5);
    }

    @Test
    void test10_getFollowingCount_shouldReturnCount() {
        when(usersRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(followRepository.countByFollower(currentUser)).thenReturn(3L);

        long count = followService.getFollowingCount(1L);

        assertThat(count).isEqualTo(3);
    }

    @Test
    void test11_getMessageSuggestions_shouldReturnMutualFollows() {
        when(usersRepository.findByKeycloakId("kc-current")).thenReturn(Optional.of(currentUser));
        when(followRepository.findMutualFollows(currentUser)).thenReturn(mutualFollows());

        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(3L);
        dto.setFirstName("Mutual");
        when(userMapper.mapEntityToDTO(mutualFollows().get(0))).thenReturn(dto);

        List<UserInfoDTO> result = followService.getMessageSuggestions("kc-current");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Mutual");
    }

    @Test
    void test12_getFollowerCount_shouldThrowEntityNotFoundException_whenUserNotFound() {
        when(usersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.getFollowerCount(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }
}
