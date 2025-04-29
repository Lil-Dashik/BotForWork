package project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.model.AddressAndTime;

import java.util.Optional;

@Repository
public interface AddressAndTimeRepository extends JpaRepository<AddressAndTime, Long> {
    Optional<AddressAndTime> findByTelegramUserId(Long telegramUserId);
}
