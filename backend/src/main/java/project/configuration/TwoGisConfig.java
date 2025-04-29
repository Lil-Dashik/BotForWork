package project.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class TwoGisConfig {
    @Value("${TwoGis.api.key}")
    private String gisKey;

    public TwoGisConfig() {
    }

    public TwoGisConfig(String gisKey) {
        this.gisKey = gisKey;
    }
}
