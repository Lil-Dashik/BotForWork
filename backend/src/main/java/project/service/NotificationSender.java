package project.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.dto.NotificationDTO;
import project.service.kafka.AlertProducer;

import java.util.List;
@Slf4j
@Component
public class NotificationSender {
    private final NotificationService notificationService;
    private final AlertProducer alertProducer;

    @Autowired
    public NotificationSender(NotificationService notificationService, AlertProducer alertProducer) {
        this.notificationService = notificationService;
        this.alertProducer = alertProducer;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void sendNotifications() {
        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsToSend();
            for (NotificationDTO notification : notifications) {
                alertProducer.sendAlert(notification);
                log.info("Отправлено уведомление для userId={}", notification.getTelegramUserId());
            }
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомлений через Kafka", e);
        }
    }
}
