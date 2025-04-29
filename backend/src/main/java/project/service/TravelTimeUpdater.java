package project.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class TravelTimeUpdater {
    private final NotificationService notificationService;

    @Autowired
    public TravelTimeUpdater(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0/5 * * * *")
    public void updateTravelTimes() {
        try {
            notificationService.updateTravelTimeIfNeeded();
            log.info("Travel time updated");
        } catch (Exception e) {
            log.error("Ошибка при обновлении времени маршрута", e);
        }
    }
}
