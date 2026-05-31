package com.example.social.app.business.service.follow.impl;

import com.example.social.app.business.dto.auth.UserInfoDTO;
import com.example.social.app.business.mapper.UserMapper;
import com.example.social.app.business.service.follow.FollowService;
import com.example.social.app.business.service.notification.NotificationService;
import com.example.social.app.db.dao.follow.FollowRepository;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.follow.FollowEntity;
import com.example.social.app.db.entity.user.UsersEntity;
import com.example.social.app.enums.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UsersRepository usersRepository;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void follow(String keycloakId, Long userId) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (currentUser.getId().equals(userId)) {
            throw new RuntimeException("Cannot follow yourself");
        }

        UsersEntity targetUser = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Target user not found"));

        if (followRepository.existsByFollowerAndFollowing(currentUser, targetUser)) {
            throw new RuntimeException("Already following this user");
        }

        FollowEntity follow = new FollowEntity();
        follow.setFollower(currentUser);
        follow.setFollowing(targetUser);
        followRepository.save(follow);

        notificationService.createNotification(
                targetUser.getId(), currentUser.getId(),
                NotificationType.FOLLOW, null,
                currentUser.getFirstName() + " started following you"
        );
    }

    @Override
    @Transactional
    public void unfollow(String keycloakId, Long userId) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UsersEntity targetUser = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Target user not found"));

        FollowEntity follow = followRepository.findByFollowerAndFollowing(currentUser, targetUser)
                .orElseThrow(() -> new RuntimeException("Not following this user"));

        followRepository.delete(follow);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(String keycloakId, Long userId) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UsersEntity targetUser = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Target user not found"));

        return followRepository.existsByFollowerAndFollowing(currentUser, targetUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserInfoDTO> getFollowers(String keycloakId, int page, int size) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return followRepository.findByFollowing(currentUser, PageRequest.of(page, size))
                .map(follow -> userMapper.mapEntityToDTO(follow.getFollower()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserInfoDTO> getFollowing(String keycloakId, int page, int size) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return followRepository.findByFollower(currentUser, PageRequest.of(page, size))
                .map(follow -> userMapper.mapEntityToDTO(follow.getFollowing()));
    }

    @Override
    @Transactional(readOnly = true)
    public long getFollowerCount(Long userId) {
        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return followRepository.countByFollowing(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserInfoDTO> getMessageSuggestions(String keycloakId) {
        UsersEntity currentUser = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<UsersEntity> mutualFollows = followRepository.findMutualFollows(currentUser);
        return mutualFollows.stream()
                .map(userMapper::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        UsersEntity user = usersRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return followRepository.countByFollower(user);
    }
}
