package com.task09;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
public class WeatherDto {

    private String id;
    private Forecast forecast;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    static class Forecast {
        private double latitude;
        private double longitude;
        private double generationtime_ms;
        private int utc_offset_seconds;
        private String timezone;
        private String timezone_abbreviation;
        private double elevation;
        private HourlyUnits hourly_units;
        private HourlyData hourly;
    }

    @Data
    static class HourlyData {
        private List<String> time;
        private List<Double> temperature_2m;
    }

    @Data
    static class HourlyUnits {
        private String time;
        private String temperature_2m;
    }
}
