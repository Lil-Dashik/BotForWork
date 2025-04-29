package project.client;


import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import project.client.request.GisRequest;

@FeignClient(name = "twoGisClient", url = "https://routing.api.2gis.com")
public interface TwoGisClient {
    @PostMapping(value = "/routing/7.0.0/global")
    @Headers("Content-Type: application/json")
    String getRoute(@RequestParam("key") String apiKey, @RequestBody GisRequest body);
}
