package com.example.social.app.business.service.sync.impl;

import com.example.social.app.business.dto.auth.UserInfoDTO;
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

        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setEmail(user.getEmail());
        userInfoDTO.setFirstName(user.getFirstName());
        userInfoDTO.setLastName(user.getLastName());
        userInfoDTO.setId(user.getId());
        userInfoDTO.setRole(user.getRole());
        return userInfoDTO;
    }

    private UsersEntity createNewUser(String keycloakId,
                                      String email,
                                      String firstName,
                                      String lastName) {

        UsersEntity user = new UsersEntity();
        user.setKeycloakId(keycloakId);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole("USER"); // local app role

        return userRepository.save(user);
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
