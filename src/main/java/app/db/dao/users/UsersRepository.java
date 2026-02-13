package app.db.dao.users;

import app.db.entity.user.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<UsersEntity, Long> {
    Optional<UsersEntity> findByEmail(String email);
}
