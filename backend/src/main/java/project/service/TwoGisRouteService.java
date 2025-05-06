package project.service;


import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.dto.Coordinates;

@Slf4j
@Service
public class TwoGisRouteService {
    private final TwoGisRequest twoGisRequest;

    @Autowired
    public TwoGisRouteService(TwoGisRequest twoGisRequest) {
        this.twoGisRequest = twoGisRequest;
    }

    public Long getRouteDurationWithTraffic(Coordinates from, Coordinates to) throws JSONException {
        return twoGisRequest.sendRequest(from, to, "jam");
    }

    public Long getRouteDurationWithoutTraffic(Coordinates from, Coordinates to) throws JSONException {
        return twoGisRequest.sendRequest(from, to, "none");
    }


}
