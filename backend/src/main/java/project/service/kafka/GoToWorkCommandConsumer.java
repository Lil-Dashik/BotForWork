package project.service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import project.dto.BotCommandDTO;
import project.dto.NotificationDTO;
import project.service.ParseService;

@Slf4j
@Service
public class GoToWorkCommandConsumer {
    private final ParseService parseService;
    private final KafkaTemplate<Long, NotificationDTO> kafkaTemplate;

    @Autowired
    public GoToWorkCommandConsumer(ParseService parseService,
                                   KafkaTemplate<Long, NotificationDTO> kafkaTemplate) {
        this.parseService = parseService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "bot-go-to-work", groupId = "service-group")
    public void handleGoToWork(BotCommandDTO dto) {
        log.info("Go to work команда: {}", dto);
        try {
            parseService.parseAndSave(dto.getTelegramId(), dto.getMessage());
            sendConfirmation(dto.getTelegramId(), "Отправим уведомление за 30 минут до выезда!");
        } catch (IllegalArgumentException e) {
            sendError(dto.getTelegramId(), e.getMessage());
        } catch (Exception e) {
            log.error("Ошибка при обработке команды go_to_work", e);
            sendError(dto.getTelegramId(), "Произошла ошибка при обработке команды. Попробуйте позже.");
        }
    }

    private void sendError(Long telegramId, String text) {
        NotificationDTO error = new NotificationDTO();
        error.setTelegramUserId(telegramId);
        error.setMessage(text);
        error.setNotifyTime(null);

        kafkaTemplate.send("confirmations", telegramId, error);
        log.warn("Отправлено сообщение об ошибке в Kafka 'confirmations': {}", text);
    }

    private void sendConfirmation(Long telegramId, String text) {
        NotificationDTO confirmation = new NotificationDTO();
        confirmation.setTelegramUserId(telegramId);
        confirmation.setMessage(text);
        confirmation.setNotifyTime(null);

        kafkaTemplate.send("confirmations", telegramId, confirmation);
        log.info("Отправлено подтверждение в Kafka 'confirmations': {}", confirmation);
    }
}
