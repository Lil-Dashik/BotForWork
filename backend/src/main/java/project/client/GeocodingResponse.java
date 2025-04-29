package project.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GeocodingResponse {
    private double geo_lat;
    private double geo_lon;
    @JsonProperty("timezone")
    private String timeZone;
}
