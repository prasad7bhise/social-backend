package com.example.social.app.business.mapper;

import com.example.social.app.business.dto.auth.UserInfoDTO;
import com.example.social.app.db.entity.user.UsersEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserInfoDTO mapEntityToDTO(UsersEntity usersEntity);

    UsersEntity mapDTOToEntity(String keycloakId, String email, String firstName, String lastName, String role);
}
