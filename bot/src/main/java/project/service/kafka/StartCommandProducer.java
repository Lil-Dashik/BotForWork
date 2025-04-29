package project.service.kafka;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import project.dto.BotCommandDTO;

@Service
public class StartCommandProducer {
    private final KafkaTemplate<Long, BotCommandDTO> kafkaTemplate;

    @Autowired
    public StartCommandProducer(KafkaTemplate<Long, BotCommandDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(BotCommandDTO dto) {
        kafkaTemplate.send("bot-start", dto.getTelegramId(), dto);
    }
}
