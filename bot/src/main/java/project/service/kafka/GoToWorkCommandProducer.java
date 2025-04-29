package project.service.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import project.dto.BotCommandDTO;

@Service
public class GoToWorkCommandProducer {
    private final KafkaTemplate<Long, BotCommandDTO> kafkaTemplate;

    @Autowired
    public GoToWorkCommandProducer(KafkaTemplate<Long, BotCommandDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(BotCommandDTO dto) {
        kafkaTemplate.send("bot-go-to-work", dto.getTelegramId(), dto);
    }
}
