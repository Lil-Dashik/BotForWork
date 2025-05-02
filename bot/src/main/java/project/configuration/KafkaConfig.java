package project.configuration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import project.dto.NotificationDTO;
import project.service.CommuteBot;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class KafkaConfig {
    private final CommuteBot commuteBot;
    @Value("${KAFKA_BROKER}")
    private String kafkaBroker;

    @Autowired
    public KafkaConfig(CommuteBot commuteBot) {
        this.commuteBot = commuteBot;
    }
    @Bean
    public ConsumerFactory<Long, NotificationDTO> notificationConsumerFactory() {
        JsonDeserializer<NotificationDTO> deserializer = new JsonDeserializer<>(NotificationDTO.class);
        deserializer.addTrustedPackages("*");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "bot-listener-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(props, new LongDeserializer(), deserializer);
    }

    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<Long, NotificationDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Long, NotificationDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationConsumerFactory());

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
