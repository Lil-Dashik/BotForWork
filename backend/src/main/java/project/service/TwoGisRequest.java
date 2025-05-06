package project.service;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.client.TwoGisClient;
import project.client.request.GisPoint;
import project.client.request.GisRequest;
import project.configuration.TwoGisConfig;
import project.dto.Coordinates;

import java.util.List;
@Slf4j
@Service
public class TwoGisRequest {
    private final TwoGisClient twoGisClient;
    private final TwoGisConfig twoGisConfig;
    @Autowired
    public TwoGisRequest(TwoGisClient twoGisClient, TwoGisConfig twoGisConfig) {
        this.twoGisClient = twoGisClient;
        this.twoGisConfig = twoGisConfig;
    }
    @RateLimiter(name = "twoGisLimiter", fallbackMethod = "fallback")
    public Long sendRequest(Coordinates from, Coordinates to, String trafficMode) throws JSONException {
        GisPoint pointFrom = new GisPoint(from.getLatitude(), from.getLongitude());
        GisPoint pointTo = new GisPoint(to.getLatitude(), to.getLongitude());
        GisRequest request = new GisRequest(List.of(pointFrom, pointTo), trafficMode);
        log.info("Запрос в 2ГИС | mode={} | data={}", trafficMode, request);
        String responseStr = twoGisClient.getRoute(twoGisConfig.getGisKey(), request);
        log.info("Ответ от 2ГИС: {}", responseStr);
        JSONObject json = new JSONObject(responseStr);
        JSONArray resultArray = json.getJSONArray("result");

        if (resultArray.length() == 0) {
            throw new RuntimeException("Маршрут не найден");
        }

        JSONObject route = resultArray.getJSONObject(0);
        int seconds = route.getInt("total_duration");
        return Math.round(seconds / 60.0);
    }
    public Long fallback(Coordinates from, Coordinates to, String trafficMode, Throwable t) {
        log.warn("Fallback 2ГИС: лимит превышен или ошибка. from={}, to={}, trafficMode={}, ошибка={}",
                from, to, trafficMode, t.toString());
        return null;
    }
}
