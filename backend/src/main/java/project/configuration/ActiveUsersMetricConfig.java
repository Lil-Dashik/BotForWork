package project.configuration;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import project.repository.UserRepository;

@Configuration
public class ActiveUsersMetricConfig {
    private final UserRepository userRepository;
    private final MeterRegistry meterRegistry;

    @Autowired
    public ActiveUsersMetricConfig(UserRepository userRepository, MeterRegistry meterRegistry) {
        this.userRepository = userRepository;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        Gauge.builder("bot.active.users", this, obj -> userRepository.countByNotificationEnabledTrue())
                .description("Количество активных пользователей бота")
                .register(meterRegistry);
    }
}
