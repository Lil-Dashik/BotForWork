package project.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class DadataConfig {
    @Value("${dadata.api.key}")
    private String dadataKey;
    @Value("${dadata.api.secret}")
    private String dadataSecret;

    public DadataConfig() {
    }

    public DadataConfig(String dadataKey, String dadataSecret) {
        this.dadataKey = dadataKey;
        this.dadataSecret = dadataSecret;
    }

}
