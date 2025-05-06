package project.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.dto.Coordinates;
import project.dto.Location;
import project.dto.UserDTO;
import project.mapper.AddressAndTimeMapper;
import project.mapper.UserMapper;
import project.model.*;

import project.repository.AddressAndTimeRepository;
import project.repository.UserRepository;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final AddressAndTimeRepository addressAndTimeRepository;
    private final UserCoordinatesService userCoordinatesService;
    private final GeocodingService geocodingService;
    private final TwoGisRouteService twoGisRouteService;
    private final AddressAndTimeMapper addressAndTimeMapper;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, AddressAndTimeRepository addressAndTimeRepository,
                       UserCoordinatesService userCoordinatesService, GeocodingService geocodingService,
                       TwoGisRouteService twoGisRouteService, AddressAndTimeMapper addressAndTimeMapper,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.addressAndTimeRepository = addressAndTimeRepository;
        this.userCoordinatesService = userCoordinatesService;
        this.geocodingService = geocodingService;
        this.twoGisRouteService = twoGisRouteService;
        this.addressAndTimeMapper = addressAndTimeMapper;
        this.userMapper = userMapper;
    }

    public void saveUserWork(UserDTO userDTO) throws JSONException {
        User user = userRepository.findByTelegramUserId(userDTO.getTelegramUserId())
                .orElseThrow(() -> new RuntimeException("User not found with telegramId=" + userDTO.getTelegramUserId()));

        String oldHomeAddress = user.getAddressAndTime() != null
                ? user.getAddressAndTime().getHomeAddress()
                : null;

        AddressAndTime existing = addressAndTimeRepository
                .findByTelegramUserId(userDTO.getTelegramUserId())
                .orElse(null);

        AddressAndTime addressAndTime = addressAndTimeMapper.toAddressAndTime(userDTO);

        if (existing != null) {
            addressAndTime.setId(existing.getId());
        }

        addressAndTimeRepository.save(addressAndTime);

        Location homeLocation = geocodingService.getCoordinates(userDTO.getHomeAddress());
        Location workLocation = geocodingService.getCoordinates(userDTO.getWorkAddress());
        if (homeLocation == null|| workLocation == null) {
            log.warn("Координаты не получены: home={}, work={}", userDTO.getHomeAddress(), userDTO.getWorkAddress());
            throw new IllegalStateException("Не удалось получить координаты из-за превышения лимита DaData");
        }
        Coordinates homeCoordinates = addressAndTimeMapper.toCoordinates(homeLocation);
        Coordinates workCoordinates = addressAndTimeMapper.toCoordinates(workLocation);
        Long travelTime = twoGisRouteService.getRouteDurationWithoutTraffic(homeCoordinates, workCoordinates);
        if (travelTime == null) {
            log.warn("Время в пути не получено из-за лимита запросов.");
            throw new IllegalStateException("Не удалось получить маршрут из-за превышения лимита 2ГИС");
        }
        updateUserWithTravelData(user, oldHomeAddress, addressAndTime, homeLocation, homeCoordinates, workCoordinates, travelTime);
    }

    private void updateUserWithTravelData(User user,
                                          String oldHomeAddress,
                                          AddressAndTime addressAndTime,
                                          Location homeLocation,
                                          Coordinates homeCoordinates,
                                          Coordinates workCoordinates,
                                          Long travelTime) {
        user.setAddressAndTime(addressAndTime);

        String newTimeZone = homeLocation.getTimeZone();
        if (timeZoneNeedsRefresh(user.getTimeZone(), oldHomeAddress, addressAndTime.getHomeAddress(), newTimeZone)) {
            user.setTimeZone(newTimeZone);
            log.info("Обновлён timeZone={} для пользователя {}", newTimeZone, user.getTelegramUserId());
        }

        UserCoordinates newCoordinates = userCoordinatesService
                .saveCoordinates(user.getTelegramUserId(), homeCoordinates, workCoordinates);

        userMapper.updateUser(user, newCoordinates, travelTime);
        userRepository.save(user);
    }

    private boolean timeZoneNeedsRefresh(String currentTimeZone, String oldAddress, String newAddress, String newTimeZone) {
        return currentTimeZone == null ||
                oldAddress == null ||
                !oldAddress.equalsIgnoreCase(newAddress) ||
                !currentTimeZone.equalsIgnoreCase(newTimeZone);
    }

    @Transactional
    public void disableNotifications(Long telegramUserId) {
        User user = userRepository.findByTelegramUserId(telegramUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setNotificationEnabled(false);
        userRepository.save(user);
    }
}
