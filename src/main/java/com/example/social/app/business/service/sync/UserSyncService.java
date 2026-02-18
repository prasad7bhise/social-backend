package com.example.social.app.business.service.sync;

import com.example.social.app.business.dto.auth.UserInfoDTO;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserSyncService {
    UserInfoDTO syncUser(Jwt jwt);
}
