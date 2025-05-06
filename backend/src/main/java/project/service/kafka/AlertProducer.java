package project.service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import project.dto.NotificationDTO;

@Service
public class AlertProducer {
    private final KafkaTemplate<Long, NotificationDTO> alertTemplate;
    private final KafkaTemplate<Long, Long> markTemplate;

    @Autowired
    public AlertProducer(
            @Qualifier("notificationKafkaTemplate") KafkaTemplate<Long, NotificationDTO> alertTemplate,
            @Qualifier("longKafkaTemplate") KafkaTemplate<Long, Long> markTemplate) {
        this.alertTemplate = alertTemplate;
        this.markTemplate = markTemplate;
    }

    public void sendAlert(NotificationDTO notification) {
        Long telegramUserId = notification.getTelegramUserId();

        alertTemplate.send("alerts", telegramUserId, notification);
    }
    public void sendMarkAsNotified(Long telegramUserId) {
        markTemplate.send("mark-notified",telegramUserId, telegramUserId);
    }
}
