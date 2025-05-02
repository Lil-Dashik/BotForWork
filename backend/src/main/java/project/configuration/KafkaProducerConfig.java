package project.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import project.dto.NotificationDTO;

@Configuration
public class KafkaProducerConfig {
    @Bean(name = "notificationKafkaTemplate")
    public KafkaTemplate<Long, NotificationDTO> notificationKafkaTemplate(ProducerFactory<Long, NotificationDTO> pf) {
        return new KafkaTemplate<>(pf);
    }

    @Bean(name = "longKafkaTemplate")
    public KafkaTemplate<Long, Long> longKafkaTemplate(ProducerFactory<Long, Long> pf) {
        return new KafkaTemplate<>(pf);
    }
}
