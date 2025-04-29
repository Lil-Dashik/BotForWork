package project.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.dto.Coordinates;
import project.dto.NotificationDTO;
import project.model.User;
import project.repository.UserRepository;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class NotificationService {
    private final UserRepository userRepository;
    private final TwoGisRouteService twoGisRouteService;

    @Autowired
    public NotificationService(UserRepository userRepository, TwoGisRouteService twoGisRouteService) {
        this.userRepository = userRepository;
        this.twoGisRouteService = twoGisRouteService;
    }

    public List<NotificationDTO> getNotificationsToSend() {
        if (isWeekend(LocalDate.now())) {
            return Collections.emptyList();
        }

        List<NotificationDTO> result = userRepository.findAll().stream()
                .filter(this::shouldNotifyUser)
                .map(user -> Map.entry(buildNotificationInfo(user.getTelegramUserId()), currentTimeInUserZone(user)))
                .filter(this::isNotificationTime)
                .map(Map.Entry::getKey)
                .toList();

        log.info("Готовим отправку уведомлений для пользователей: {}",
                result.stream().map(NotificationDTO::getTelegramUserId).toList());

        return result;
    }

    private boolean shouldNotifyUser(User user) {
        return user.isNotificationEnabled()
                && (user.getLastNotificationSent() == null || !user.getLastNotificationSent().isEqual(LocalDate.now()))
                && isUserProfileComplete(user);
    }

    private boolean isUserProfileComplete(User user) {
        return user.getAddressAndTime() != null &&
                user.getAddressAndTime().getWorkStartTime() != null &&
                user.getTimeZone() != null &&
                user.getCurrentTravelTime() != null;
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private ZonedDateTime currentTimeInUserZone(User user) {
        return ZonedDateTime.now(ZoneId.of(user.getTimeZone()));
    }

    private boolean isNotificationTime(Map.Entry<NotificationDTO, ZonedDateTime> entry) {
        ZonedDateTime notifyTime = entry.getKey().getNotifyTime();
        ZonedDateTime now = entry.getValue();
        long diff = Duration.between(notifyTime, now).toMinutes();

        log.info("Проверка уведомления для userId={} | notifyTime={} | now={} | разница={} мин",
                entry.getKey().getTelegramUserId(), notifyTime, now, diff);

        if (notifyTime.isBefore(now.minusMinutes(15))) {
            log.warn("Слишком раннее уведомление для userId={}, notifyTime={}, now={}",
                    entry.getKey().getTelegramUserId(), notifyTime, now);
            return false;
        }

        return diff >= -2 && diff <= 30;
    }

    public NotificationDTO buildNotificationInfo(Long telegramId) {
        User user = userRepository.findByTelegramUserId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found with telegramId = " + telegramId));

        ZoneId userZone = ZoneId.of(user.getTimeZone());
        LocalTime workStart = user.getAddressAndTime().getWorkStartTime();
        Long travelTime = user.getCurrentTravelTime();

        Duration totalOffset = Duration.ofMinutes(travelTime + 30);
        LocalDate today = LocalDate.now(userZone);
        ZonedDateTime notifyZdt = ZonedDateTime.of(today, workStart, userZone).minus(totalOffset);

        String message = String.format("Через 30 минут нужно выходить, чтобы успеть к %s", workStart);

        return new NotificationDTO(user.getTelegramUserId(), message, notifyZdt);
    }

    public void updateTravelTimeIfNeeded() {
        userRepository.findAll().stream()
                .filter(this::shouldUpdateTravelTime)
                .forEach(this::updateUserTravelTime);
    }

    private boolean shouldUpdateTravelTime(User user) {
        return user.isNotificationEnabled()
                && user.getAddressAndTime() != null
                && user.getUserCoordinates() != null;
    }

    private void updateUserTravelTime(User user) {
        ZoneId zoneId = ZoneId.of(user.getTimeZone());
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalTime workStart = user.getAddressAndTime().getWorkStartTime();

        Long baseTravelTime = user.getMinObservedTravelTime();
        if (baseTravelTime == null) baseTravelTime = user.getCurrentTravelTime();
        if (baseTravelTime == null) {
            initializeTravelTime(user);
            return;
        }

        ZonedDateTime expectedCheckTime = ZonedDateTime.of(
                now.toLocalDate(),
                workStart.minusMinutes(baseTravelTime).minusMinutes(60),
                zoneId
        );

        long diffMinutes = Duration.between(expectedCheckTime, now).toMinutes();
        log.info("Пользователь {}: плановая проверка в {} (разница {} мин)",
                user.getTelegramUserId(), expectedCheckTime, diffMinutes);

        if (diffMinutes >= -30 && diffMinutes <= 2) {
            Coordinates from = user.getUserCoordinates().getHomeCoordinates();
            Coordinates to = user.getUserCoordinates().getWorkCoordinates();
            try {
                long updatedMinutes = twoGisRouteService.getRouteDurationWithTraffic(from, to);

                log.info("Обновляем currentTravelTime для пользователя {}: было {} мин, стало {} мин",
                        user.getTelegramUserId(), user.getCurrentTravelTime(), updatedMinutes);

                if (updatedMinutes > baseTravelTime) {
                    ZonedDateTime newNotifyTime = ZonedDateTime.of(
                            now.toLocalDate(),
                            workStart.minusMinutes(updatedMinutes).minusMinutes(30),
                            zoneId
                    );


                    log.info("Время в пути увеличилось, новое время оповещения для пользователя {}: {}",
                            user.getTelegramUserId(), newNotifyTime);
                }
                user.setCurrentTravelTime(updatedMinutes);

                Long minObserved = user.getMinObservedTravelTime();
                if (minObserved == null || updatedMinutes < minObserved) {
                    user.setMinObservedTravelTime(updatedMinutes);
                    log.info("Обновлено минимальное travelTime: {} мин", updatedMinutes);
                }

                userRepository.save(user);
            } catch (JSONException e) {
                log.error("Ошибка при получении маршрута от 2ГИС: {}", e.getMessage());
            }
        }
    }
    private void initializeTravelTime(User user) {
        log.info("У пользователя {} нет времени поездки. Запрашиваем маршрут впервые...", user.getTelegramUserId());
        try {
            Coordinates from = user.getUserCoordinates().getHomeCoordinates();
            Coordinates to = user.getUserCoordinates().getWorkCoordinates();
            long initialTravelTime = twoGisRouteService.getRouteDurationWithTraffic(from, to);

            user.setCurrentTravelTime(initialTravelTime);
            user.setMinObservedTravelTime(initialTravelTime);

            log.info(" Рассчитано первое время в пути для пользователя {}: {} мин", user.getTelegramUserId(), initialTravelTime);

            userRepository.save(user);
        } catch (JSONException e) {
            log.error(" Ошибка при получении маршрута от 2ГИС для нового пользователя: {}", e.getMessage());
        }
    }
}



