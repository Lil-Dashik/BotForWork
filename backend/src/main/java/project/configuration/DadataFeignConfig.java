package project.configuration;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DadataFeignConfig {
    private final DadataConfig dadataConfig;

    public DadataFeignConfig(DadataConfig dadataConfig) {
        this.dadataConfig = dadataConfig;
    }

    @Bean
    public RequestInterceptor dadataRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "Token " + dadataConfig.getDadataKey());
            requestTemplate.header("X-Secret", dadataConfig.getDadataSecret());
            requestTemplate.header("Content-Type", "application/json");
            requestTemplate.header("Accept", "application/json");
        };
    }
}
