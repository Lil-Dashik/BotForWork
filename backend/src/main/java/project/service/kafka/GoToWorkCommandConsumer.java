package project.service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import project.dto.BotCommandDTO;
import project.dto.NotificationDTO;
import project.mapper.NotificationMapper;
import project.service.ParseService;

@Slf4j
@Service
public class GoToWorkCommandConsumer {
    private final ParseService parseService;
    private final KafkaTemplate<Long, NotificationDTO> kafkaTemplate;
    private final NotificationMapper notificationMapper;

    @Autowired
    public GoToWorkCommandConsumer(ParseService parseService,
                                   @Qualifier("notificationKafkaTemplate") KafkaTemplate<Long, NotificationDTO> kafkaTemplate,
                                   NotificationMapper notificationMapper) {
        this.parseService = parseService;
        this.kafkaTemplate = kafkaTemplate;
        this.notificationMapper = notificationMapper;
    }

    @KafkaListener(
            topics = "bot-go-to-work",
            groupId = "service-group",
            containerFactory = "botCommandKafkaListenerFactory"
    )
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
        NotificationDTO error = notificationMapper.createSimpleNotification(telegramId, text);

        kafkaTemplate.send("confirmations", telegramId, error);
        log.warn("Отправлено сообщение об ошибке в Kafka 'confirmations': {}", text);
    }

    private void sendConfirmation(Long telegramId, String text) {
        NotificationDTO confirmation = notificationMapper.createSimpleNotification(telegramId, text);

        kafkaTemplate.send("confirmations", telegramId, confirmation);
        log.info("Отправлено подтверждение в Kafka 'confirmations': {}", confirmation);
    }
}
