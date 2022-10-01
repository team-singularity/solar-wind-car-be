package com.nasa.solarwindcar.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SolarWinds {
    List<SolarWind> solarWindFirst;
    List<SolarWind> solarWindSecond;
}
