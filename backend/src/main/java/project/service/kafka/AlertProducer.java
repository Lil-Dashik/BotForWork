package project.service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import project.dto.NotificationDTO;

@Service
public class AlertProducer {
    private final KafkaTemplate<Long, NotificationDTO> kafkaTemplate;

    @Autowired
    public AlertProducer(KafkaTemplate<Long, NotificationDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendAlert(NotificationDTO notification) {
        kafkaTemplate.send("alerts", notification.getTelegramUserId(), notification);
    }
}
