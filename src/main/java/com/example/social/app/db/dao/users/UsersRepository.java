package com.example.social.app.db.dao.users;

import com.example.social.app.db.entity.user.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<UsersEntity, Long> {
    Optional<UsersEntity> findByEmail(String email);

    Optional<UsersEntity> findByKeycloakId(String keycloakId);

    @Query("SELECT u FROM UsersEntity u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY u.firstName ASC")
    List<UsersEntity> searchByName(@Param("q") String q);
}
