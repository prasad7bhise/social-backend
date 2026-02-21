package com.example.social.app.business.service.sync.impl;

import com.example.social.app.business.dto.auth.UserInfoDTO;
import com.example.social.app.business.mapper.UserMapper;
import com.example.social.app.business.service.sync.UserSyncService;
import com.example.social.app.db.dao.users.UsersRepository;
import com.example.social.app.db.entity.user.UsersEntity;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class UserSyncServiceImpl implements UserSyncService {

    private final UsersRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserInfoDTO syncUser(Jwt jwt) {

        String keycloakId = jwt.getSubject();
        String email = jwt.getClaim("email");
        String firstName = jwt.getClaim("given_name");
        String lastName = jwt.getClaim("family_name");

        UsersEntity user = userRepository.findByKeycloakId(keycloakId)
                .map(existingUser ->
                        updateIfChanged(existingUser, email, firstName, lastName))
                .orElseGet(() ->
                        createNewUser(keycloakId, email, firstName, lastName));

        return userMapper.mapEntityToDTO(user);
    }

    private UsersEntity createNewUser(String keycloakId,
                                      String email,
                                      String firstName,
                                      String lastName) {

        return userRepository.save(userMapper.mapDTOToEntity(keycloakId, email, firstName, lastName, "USER"));
    }

    private UsersEntity updateIfChanged(UsersEntity user,
                                        String email,
                                        String firstName,
                                        String lastName) {

        boolean changed = false;

        if (!Objects.equals(user.getEmail(), email)) {
            user.setEmail(email);
            changed = true;
        }

        if (!Objects.equals(user.getFirstName(), firstName)) {
            user.setFirstName(firstName);
            changed = true;
        }

        if (!Objects.equals(user.getLastName(), lastName)) {
            user.setLastName(lastName);
            changed = true;
        }
        return changed ? userRepository.save(user) : user;
    }
}
