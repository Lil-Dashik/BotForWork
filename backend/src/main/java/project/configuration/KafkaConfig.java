package project.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaConfig {
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
}
