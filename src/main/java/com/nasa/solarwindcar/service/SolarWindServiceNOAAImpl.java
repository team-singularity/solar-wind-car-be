package com.nasa.solarwindcar.service;

import com.nasa.solarwindcar.model.SolarWind;
import com.nasa.solarwindcar.model.SolarWinds;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service("NOAA")
public class SolarWindServiceNOAAImpl implements SolarWindService {

    RestTemplate http;

    @Autowired
    public SolarWindServiceNOAAImpl(RestTemplate http) {
        this.http = http;
    }

    public SolarWinds fetchSolarWind(String date1, String date2) {
        String url = "https://services.swpc.noaa.gov/products/solar-wind/plasma-7-day.json";
        String[][] solarWinds = http.getForObject(url, String[][].class);

        for (String[] solarWind : solarWinds) {
            SolarWind.builder()
                    .time(solarWind[0])
                    .bulkSpeed(Double.valueOf(solarWind[2]))
                    .ionTemperature(Double.valueOf(solarWind[3]))
                    .build();
        }

        log.info("solarWinds: " + solarWinds[0][0]);
        return null;
    }
}
