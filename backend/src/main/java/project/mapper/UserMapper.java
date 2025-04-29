package project.mapper;

import org.springframework.stereotype.Component;
import project.dto.UserDTO;
import project.model.User;
import project.model.UserCoordinates;

import java.time.LocalTime;

@Component
public class UserMapper {
    public void updateUser(User user, UserCoordinates coordinates, Long travelTime) {
        user.setUserCoordinates(coordinates);
        user.setCurrentTravelTime(travelTime);
        user.setMinObservedTravelTime(travelTime);
        user.setLastNotificationSent(null);
    }

    public User toNewUser(Long telegramId, String username, String firstName) {
        User user = new User();
        user.setTelegramUserId(telegramId);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setNotificationEnabled(true);
        return user;
    }

    public UserDTO toUserDTO(Long telegramId, String home, String work, LocalTime time) {
        UserDTO dto = new UserDTO();
        dto.setTelegramUserId(telegramId);
        dto.setHomeAddress(home);
        dto.setWorkAddress(work);
        dto.setWorkStartTime(time);
        return dto;
    }
}
