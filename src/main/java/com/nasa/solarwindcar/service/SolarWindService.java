package com.nasa.solarwindcar.service;

import com.nasa.solarwindcar.model.SolarWinds;

public interface SolarWindService {
    SolarWinds fetchSolarWind(String date1, String date2);

}
