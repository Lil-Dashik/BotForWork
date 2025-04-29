package project.mapper;

import org.springframework.stereotype.Component;
import project.dto.Coordinates;
import project.dto.Location;
import project.dto.UserDTO;
import project.model.AddressAndTime;

@Component
public class AddressAndTimeMapper {
    public AddressAndTime toAddressAndTime(UserDTO dto) {
        AddressAndTime addressAndTime = new AddressAndTime();
        addressAndTime.setTelegramUserId(dto.getTelegramUserId());
        addressAndTime.setHomeAddress(dto.getHomeAddress());
        addressAndTime.setWorkAddress(dto.getWorkAddress());
        addressAndTime.setWorkStartTime(dto.getWorkStartTime());
        return addressAndTime;
    }

    public Coordinates toCoordinates(Location location) {
        return new Coordinates(location.getCoordinatesLat(), location.getCoordinatesLon());
    }

}
