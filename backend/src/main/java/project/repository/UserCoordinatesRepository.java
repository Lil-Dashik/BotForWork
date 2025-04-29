package project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.model.UserCoordinates;

import java.util.Optional;


@Repository
public interface UserCoordinatesRepository extends JpaRepository<UserCoordinates, Long> {
    Optional<UserCoordinates> findByTelegramUserId(Long telegramUserId);
}
