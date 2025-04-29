package project.mapper;

import org.springframework.stereotype.Component;
import project.dto.Coordinates;
import project.model.UserCoordinates;

@Component
public class UserCoordinatesMapper {
    public UserCoordinates toUserCoordinates(Long telegramUserId, Coordinates homeCoordinates, Coordinates workCoordinates) {
        UserCoordinates userCoordinates = new UserCoordinates();
        userCoordinates.setTelegramUserId(telegramUserId);
        userCoordinates.setHomeLatitude(homeCoordinates.getLatitude());
        userCoordinates.setHomeLongitude(homeCoordinates.getLongitude());
        userCoordinates.setWorkLatitude(workCoordinates.getLatitude());
        userCoordinates.setWorkLongitude(workCoordinates.getLongitude());
        return userCoordinates;
    }

    public UserCoordinates updateUserCoordinates(UserCoordinates userCoordinates, Coordinates homeCoordinates, Coordinates workCoordinates) {
        userCoordinates.setHomeLatitude(homeCoordinates.getLatitude());
        userCoordinates.setHomeLongitude(homeCoordinates.getLongitude());
        userCoordinates.setWorkLatitude(workCoordinates.getLatitude());
        userCoordinates.setWorkLongitude(workCoordinates.getLongitude());
        return userCoordinates;
    }
}
