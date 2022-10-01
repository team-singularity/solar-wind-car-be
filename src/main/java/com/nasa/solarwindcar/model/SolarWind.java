package com.nasa.solarwindcar.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SolarWind {
    String time;
    Integer bulkSpeed;
    Float ionTemperature;
    boolean isGood;
}
