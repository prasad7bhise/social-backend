package com.example.social.app.business.service.follow;

import com.example.social.app.business.dto.auth.UserInfoDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface FollowService {
    void follow(String keycloakId, Long userId);

    void unfollow(String keycloakId, Long userId);

    boolean isFollowing(String keycloakId, Long userId);

    Page<UserInfoDTO> getFollowers(String keycloakId, int page, int size);

    Page<UserInfoDTO> getFollowing(String keycloakId, int page, int size);

    long getFollowerCount(Long userId);

    long getFollowingCount(Long userId);

    List<UserInfoDTO> getMessageSuggestions(String keycloakId);
}
