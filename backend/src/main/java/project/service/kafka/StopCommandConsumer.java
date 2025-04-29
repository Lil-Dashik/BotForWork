package project.service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import project.dto.BotCommandDTO;
import project.service.UserService;

@Service
@Slf4j
public class StopCommandConsumer {
    private final UserService userService;
    @Autowired
    public StopCommandConsumer(UserService userService) {
        this.userService = userService;
    }
    @KafkaListener(topics = "bot-stop", groupId = "service-group")
    public void handleStop(BotCommandDTO dto) {
        log.info("Stop команда: {}", dto);
        userService.disableNotifications(dto.getTelegramId());
    }
}
