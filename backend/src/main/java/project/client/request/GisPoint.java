package project.client.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class GisPoint {
    private String type = "stop";
    private double lat;
    private double lon;

    public GisPoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }
}
