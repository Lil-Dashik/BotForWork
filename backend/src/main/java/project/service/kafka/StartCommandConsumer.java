package project.service.kafka;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import project.dto.BotCommandDTO;
import project.service.ParseService;

@Slf4j
@Service
public class StartCommandConsumer {
    private final ParseService parseService;

    @Autowired
    public StartCommandConsumer(ParseService parseService) {
        this.parseService = parseService;
    }

    @KafkaListener(
            topics = "bot-start",
            groupId = "service-group",
            containerFactory = "botCommandKafkaListenerFactory"
    )
    public void handleStart(BotCommandDTO dto) {
        log.info("Start команда: {}", dto);
        parseService.parseAndSaveUser(dto.getTelegramId() + "; " + dto.getMessage());
    }
}
