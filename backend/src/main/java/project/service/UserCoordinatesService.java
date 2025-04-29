package project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.dto.Coordinates;
import project.mapper.UserCoordinatesMapper;
import project.model.UserCoordinates;
import project.repository.UserCoordinatesRepository;

import java.util.Optional;

@Service
public class UserCoordinatesService {
    private final UserCoordinatesRepository userCoordinatesRepository;
    private final UserCoordinatesMapper userCoordinatesMapper;


    @Autowired
    public UserCoordinatesService(UserCoordinatesRepository userCoordinatesRepository,
                                  UserCoordinatesMapper userCoordinatesMapper) {
        this.userCoordinatesRepository = userCoordinatesRepository;
        this.userCoordinatesMapper = userCoordinatesMapper;
    }

    public UserCoordinates saveCoordinates(Long telegramUserId, Coordinates homeCoordinates, Coordinates workCoordinates) {
        Optional<UserCoordinates> existing = userCoordinatesRepository.findByTelegramUserId(telegramUserId);

        UserCoordinates updated = existing
                .map(existingEntity -> userCoordinatesMapper.updateUserCoordinates(existingEntity, homeCoordinates, workCoordinates))
                .orElseGet(() -> userCoordinatesMapper.toUserCoordinates(telegramUserId, homeCoordinates, workCoordinates));

        return userCoordinatesRepository.save(updated);
    }
}
