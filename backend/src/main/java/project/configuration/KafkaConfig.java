package project.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import project.dto.BotCommandDTO;
import project.dto.NotificationDTO;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@Configuration
public class KafkaConfig {
    private final  KafkaProperties kafkaProperties;

    private final KafkaTemplate<Long, NotificationDTO> notificationKafkaTemplate;
    @Autowired
    public KafkaConfig(@Qualifier("notificationKafkaTemplate") KafkaTemplate<Long, NotificationDTO> notificationKafkaTemplate,
                       KafkaProperties kafkaProperties){
        this.notificationKafkaTemplate = notificationKafkaTemplate;
        this.kafkaProperties = kafkaProperties;
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
    public ConsumerFactory<Long, BotCommandDTO> botCommandConsumerFactory() {
        JsonDeserializer<BotCommandDTO> deserializer = new JsonDeserializer<>(BotCommandDTO.class);
        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.getConsumer().buildProperties(null),
                new LongDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConsumerFactory<Long, NotificationDTO> notificationConsumerFactory() {
        JsonDeserializer<NotificationDTO> deserializer = new JsonDeserializer<>(NotificationDTO.class);
        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.getConsumer().buildProperties(null),
                new LongDeserializer(),
                deserializer
        );
    }

    @Bean(name = "botCommandKafkaListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<Long, BotCommandDTO> botCommandKafkaListenerFactory(
            @Qualifier("botCommandConsumerFactory") ConsumerFactory<Long, BotCommandDTO> consumerFactory) {
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

                        notificationKafkaTemplate.send("confirmations", telegramId, errorMsg);
                    }

                    log.warn("Ошибка при обработке сообщения Kafka: {}", exception.getMessage());
                },
                new FixedBackOff(0L, 0)
        );

        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean(name = "notificationKafkaListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<Long, NotificationDTO> notificationKafkaListenerFactory(
            @Qualifier("notificationConsumerFactory") ConsumerFactory<Long, NotificationDTO> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, NotificationDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

}
