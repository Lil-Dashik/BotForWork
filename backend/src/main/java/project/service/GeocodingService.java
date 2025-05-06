package project.service;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.client.DadataClient;
import project.client.GeocodingResponse;

import project.dto.Location;

import java.util.List;
@Slf4j
@Service
public class GeocodingService {
    private final DadataClient dadataClient;


    @Autowired
    public GeocodingService(DadataClient dadataClient) {
        this.dadataClient = dadataClient;
    }

    @RateLimiter(name = "dadataLimiter", fallbackMethod = "fallback")
    public Location getCoordinates(String address) {
        log.info("Отправка запроса в DaData для адреса: {}", address);
        List<GeocodingResponse> responses = dadataClient.cleanAddress(List.of(address));

        if (responses != null && !responses.isEmpty()) {
            GeocodingResponse response = responses.get(0);
            log.info("Получен ответ DaData: {}", response);
            return new Location(response.getGeo_lat(), response.getGeo_lon(), response.getTimeZone());
        }

        log.warn("Пустой ответ DaData для адреса: {}", address);
        return null;
    }

    public Location fallback(String address, Throwable t) {
        log.warn("Rate limit exceeded или ошибка при вызове DaData: {}", t.toString());
        return null;
    }
}
