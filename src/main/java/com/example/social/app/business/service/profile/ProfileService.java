package com.example.social.app.business.service.profile;

import com.example.social.app.business.dto.auth.ProfileUpdateRequest;
import com.example.social.app.business.dto.auth.UserInfoDTO;

import java.util.List;

public interface ProfileService {
    UserInfoDTO getProfile(String keycloakId);

    UserInfoDTO getProfileByDbId(Integer userId);

    UserInfoDTO updateProfile(String keycloakId, ProfileUpdateRequest request);

    List<UserInfoDTO> searchUsers(String query);
}
