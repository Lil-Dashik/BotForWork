package project.client.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class GisRequest {
    private List<GisPoint> points;
    @JsonProperty("traffic_mode")
    private String trafficMode;

    public GisRequest(List<GisPoint> points, String trafficMode) {

        this.points = points;
        this.trafficMode = "jam";
    }
}
