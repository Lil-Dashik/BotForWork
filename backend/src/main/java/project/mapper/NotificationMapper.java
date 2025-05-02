package project.mapper;

import org.springframework.stereotype.Component;
import project.dto.NotificationDTO;

@Component
public class NotificationMapper {
    public NotificationDTO createSimpleNotification(Long telegramUserId, String message) {
        NotificationDTO dto = new NotificationDTO();
        dto.setTelegramUserId(telegramUserId);
        dto.setMessage(message);
        dto.setNotifyTime(null);
        return dto;
    }
}
