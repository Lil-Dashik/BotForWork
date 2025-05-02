package project.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import project.dto.BotCommandDTO;
import project.dto.NotificationDTO;

import java.util.Map;
@Slf4j
@Configuration
public class KafkaConfig {
    private final KafkaTemplate<Long, NotificationDTO> kafkaTemplate;
    @Autowired
    public KafkaConfig(KafkaTemplate<Long, NotificationDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Bean
    public NewTopic startCommandTopic() {
        return TopicBuilder.name("bot-start")
                .partitions(3)
                .replicas(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

    @Bean
    public NewTopic goToWorkCommandTopic() {
        return TopicBuilder.name("bot-go-to-work")
                .partitions(3)
                .replicas(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

    @Bean
    public NewTopic stopCommandTopic() {
        return TopicBuilder.name("bot-stop")
                .partitions(3)
                .replicas(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

    @Bean
    public NewTopic alertsTopic() {
        return TopicBuilder.name("alerts")
                .partitions(3)
                .replicas(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

    @Bean
    public NewTopic markNotifiedTopic() {
        return TopicBuilder.name("mark-notified")
                .partitions(3)
                .replicas(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }

    @Bean
    public NewTopic confirmationsTopic() {
        return TopicBuilder.name("confirmations")
                .partitions(3)
                .replicas(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }
    @Bean
    public KafkaTemplate<Long, Long> longKafkaTemplate(ProducerFactory<Long, Long> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<Long, BotCommandDTO> kafkaListenerContainerFactory(
            ConsumerFactory<Long, BotCommandDTO> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, BotCommandDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (consumerRecord, exception) -> {
                    if (consumerRecord.key() instanceof Long telegramId) {
                        NotificationDTO errorMsg = new NotificationDTO();
                        errorMsg.setTelegramUserId(telegramId);
                        errorMsg.setMessage("Ошибка в формате команды. Убедитесь, что вы отправили: <дом>; <работа>; <время>");
                        errorMsg.setNotifyTime(null);

                        kafkaTemplate.send("confirmations", telegramId, errorMsg);
                    }

                    log.warn("Ошибка при обработке сообщения Kafka: {}", exception.getMessage());
                },
                new FixedBackOff(0L, 0)
        );

        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
