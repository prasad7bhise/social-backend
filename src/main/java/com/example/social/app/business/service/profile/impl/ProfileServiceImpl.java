package com.example.social.app.business.service.profile.impl;

import com.example.social.app.business.dto.auth.ProfileUpdateRequest;
import com.example.social.app.business.dto.auth.UserInfoDTO;
import com.example.social.app.business.mapper.UserMapper;
import com.example.social.app.business.service.keycloak.KeycloakAdminService;
import com.example.social.app.business.service.profile.ProfileService;
import com.example.social.app.db.dao.post.PostRepository;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.user.UsersEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final UsersRepository usersRepository;
    private final UserMapper userMapper;
    private final KeycloakAdminService keycloakAdminService;
    private final PostRepository postRepository;

    @Override
    @Transactional(readOnly = true)
    public UserInfoDTO getProfile(String keycloakId) {
        UsersEntity user = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        long postCount = postRepository.countByUserId(user.getId());
        return userMapper.mapEntityToDTO(user, postCount);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoDTO getProfileByDbId(Integer userId) {
        UsersEntity user = usersRepository.findById(userId.longValue())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        long postCount = postRepository.countByUserId(user.getId());
        return userMapper.mapEntityToDTO(user, postCount);
    }

    @Override
    @Transactional
    public UserInfoDTO updateProfile(String keycloakId, ProfileUpdateRequest request) {
        UsersEntity user = usersRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Update Keycloak asynchronously (best-effort — local DB update succeeds regardless)
        keycloakAdminService.updateUser(keycloakId, request);

        // Update local DB
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        usersRepository.save(user);
        return userMapper.mapEntityToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserInfoDTO> searchUsers(String query) {
        return usersRepository.searchByName(query)
                .stream()
                .map(userMapper::mapEntityToDTO)
                .collect(Collectors.toList());
    }
}
