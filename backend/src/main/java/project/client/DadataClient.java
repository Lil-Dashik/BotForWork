package project.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import project.configuration.DadataFeignConfig;

import java.util.List;

@FeignClient(name = "dadataClient", url = "https://cleaner.dadata.ru", configuration = DadataFeignConfig.class)
public interface DadataClient {
    @PostMapping(value = "/api/v1/clean/address", consumes = MediaType.APPLICATION_JSON_VALUE)
    List<GeocodingResponse> cleanAddress(@RequestBody List<String> addresses);
}
