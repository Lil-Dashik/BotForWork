package project.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import project.dto.NotificationDTO;
import project.service.CommuteBot;


@Configuration
public class KafkaConfig {
    private final CommuteBot commuteBot;

    @Autowired
    public KafkaConfig(CommuteBot commuteBot) {
        this.commuteBot = commuteBot;
    }

    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<Long, NotificationDTO> kafkaListenerContainerFactory(
            ConsumerFactory<Long, NotificationDTO> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<Long, NotificationDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> {
                    Long telegramId = (Long) record.key();
                    commuteBot.sendMessage(telegramId, "Ошибка при получении уведомления. Попробуйте позже.");
                },
                new FixedBackOff(0, 0)
        );

        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
