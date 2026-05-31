package com.example.social.app.business.mapper;

import com.example.social.app.business.dto.auth.UserInfoDTO;
import com.example.social.app.db.entity.user.UsersEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface UserMapper {
    UserInfoDTO mapEntityToDTO(UsersEntity usersEntity);

    default UserInfoDTO mapEntityToDTO(UsersEntity user, long postCount) {
        UserInfoDTO dto = mapEntityToDTO(user);
        dto.setPostCount(postCount);
        return dto;
    }

    UsersEntity mapDTOToEntity(String keycloakId, String email, String firstName, String lastName, String role);
}
