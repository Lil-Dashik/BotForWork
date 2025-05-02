package project.service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.repository.UserRepository;

import java.time.LocalDate;

@Service
public class NotificationMarkService {
    private final UserRepository userRepository;

    public NotificationMarkService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @KafkaListener(
            topics = "mark-notified",
            groupId = "notification-mark-group",
            containerFactory = "notificationKafkaListenerFactory"
    )
    public void handleMarkNotified(Long telegramUserId) {
        userRepository.findByTelegramUserId(telegramUserId).ifPresent(user -> {
            user.setLastNotificationSent(LocalDate.now());
            userRepository.save(user);
        });
    }
}
