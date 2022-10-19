package com.nasa.solarwindcar.service;

import com.nasa.solarwindcar.model.SolarWind;
import com.nasa.solarwindcar.model.SolarWinds;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("NASA")
public class SolarWindServiceNASAImpl implements SolarWindService {

    public SolarWinds fetchSolarWind(String date1, String date2) {

        List<SolarWind> solarWind1 = new ArrayList<>();
        List<SolarWind> solarWind2 = new ArrayList<>();
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            String url1 = "https://sohoftp.nascom.nasa.gov/sdb/goes/ace/daily/" + date1 + "_ace_swepam_1m.txt";
            String url2 = "https://sohoftp.nascom.nasa.gov/sdb/goes/ace/daily/" + date2 + "_ace_swepam_1m.txt";
            HttpGet request1 = new HttpGet(url1);
            HttpGet request2 = new HttpGet(url2);
            HttpResponse response1 = client.execute(request1);
            HttpResponse response2 = client.execute(request2);
            HttpEntity entity1 = response1.getEntity();
            HttpEntity entity2 = response2.getEntity();
            BufferedReader in1 = new BufferedReader((new InputStreamReader(entity1.getContent())));
            String input1;
            while ((input1 = in1.readLine()) != null) {
                if (input1.startsWith("2")) {
                    SolarWind solarWind = parseInputToSolarWind(input1);
                    solarWind1.add(solarWind);
                }
            }
            in1.close();
            BufferedReader in2 = new BufferedReader((new InputStreamReader(entity2.getContent())));
            String input2;
            while ((input2 = in2.readLine()) != null) {
                if (input2.startsWith("2")) {
                    SolarWind solarWind = parseInputToSolarWind(input2);
                    solarWind2.add(solarWind);
                }
            }
        } catch (Exception e) {
            log.error("Could not access files : " + e.getMessage());
        }
        SolarWinds solarWinds = SolarWinds.builder()
                .solarWindFirst(solarWind1)
                .solarWindSecond(solarWind2)
                .build();
        editSolarWindsSize(solarWinds);
        addWinner(solarWinds);
        addTemperatureLevel(solarWinds);
        return solarWinds;
    }

    private SolarWind parseInputToSolarWind(String input) {
        String[] columns = input.split("\\s+");

        return SolarWind.builder()
                .time(columns[3])
                .bulkSpeed(Double.parseDouble(columns[8]))
                .ionTemperature(Double.parseDouble(columns[9]))
                .isGood(Integer.parseInt(columns[6]) == 0 && !columns[8].equals("-9999.9") && !columns[9].equals("-1.00e+05"))
                .build();
    }

    private SolarWinds editSolarWindsSize(SolarWinds solarWinds) {
        int countRowsFirst = solarWinds.getSolarWindFirst().size();
        int countRowsSecond = solarWinds.getSolarWindSecond().size();
        if (countRowsFirst < countRowsSecond) {
            solarWinds.setSolarWindSecond(solarWinds.getSolarWindSecond().subList(0, countRowsFirst));
        } else if (countRowsSecond < countRowsFirst) {
            solarWinds.setSolarWindFirst(solarWinds.getSolarWindFirst().subList(0, countRowsSecond));
        }
        if (solarWinds.getSolarWindFirst().size() == solarWinds.getSolarWindSecond().size()) {
            return solarWinds;
        }
        throw new RuntimeException("Different sizes of solarWind lists.");
    }

    private SolarWinds addWinner(SolarWinds solarWinds) {
        // solar wind speed sum
        Double speedFirst = solarWinds.getSolarWindFirst().stream()
                .filter(solarWind -> solarWind.isGood())
                .mapToDouble(solarWind -> solarWind.getBulkSpeed()).sum();
        //log.info(String.valueOf(speedFirst));

        Double speedSecond = solarWinds.getSolarWindSecond().stream()
                .filter(solarWind -> solarWind.isGood())
                .mapToDouble(solarWind -> solarWind.getBulkSpeed()).sum();
        //log.info(String.valueOf(speedSecond));

        if (speedFirst > speedSecond) {
            solarWinds.setWinner(1);
        } else if (speedSecond > speedFirst) {
            solarWinds.setWinner(2);
        } else {
            solarWinds.setWinner(0);
        }
        return solarWinds;
    }

    private void addTemperatureLevel(SolarWinds solarWinds) {
        // get maximum temperature
        Double maxTempFirst = solarWinds.getSolarWindFirst().stream()
                .filter(solarWind -> solarWind.isGood())
                .max(Comparator.comparing(SolarWind::getIonTemperature))
                .map(solarWind -> solarWind.getIonTemperature()).orElseThrow();
        Double maxTempSecond = solarWinds.getSolarWindSecond().stream()
                .filter(solarWind -> solarWind.isGood())
                .max(Comparator.comparing(SolarWind::getIonTemperature))
                .map(solarWind -> solarWind.getIonTemperature()).orElseThrow();
        Double maxTemp = 0D;
        if (maxTempFirst >= maxTempSecond) {
            maxTemp += maxTempFirst;
        } else {
            maxTemp += maxTempSecond;
        }
        log.info(String.valueOf(maxTemp));

        // get minimum temperature
        Double minTempFirst = solarWinds.getSolarWindFirst().stream()
                .filter(solarWind -> solarWind.isGood())
                .min(Comparator.comparing(SolarWind::getIonTemperature))
                .map(solarWind -> solarWind.getIonTemperature()).orElseThrow();
        Double minTempSecond = solarWinds.getSolarWindSecond().stream()
                .filter(solarWind -> solarWind.isGood())
                .min(Comparator.comparing(SolarWind::getIonTemperature))
                .map(solarWind -> solarWind.getIonTemperature()).orElseThrow();
        Double minTemp = 0D;
        if (minTempFirst <= minTempSecond) {
            minTemp += minTempFirst;
        } else {
            minTemp += minTempSecond;
        }
        //log.info(String.valueOf(minTemp));

        Double scale = maxTemp - minTemp;
        List<Double> upperLevels = new ArrayList<>();
        //log.info(String.valueOf(upperLevels));
        for (int i = 1; i <= maxTemp; i++) {
            upperLevels.add(scale/5 * i);
        }
        solarWinds.getSolarWindFirst().stream()
                .filter(solarWind -> solarWind.isGood())
                .forEach(solarWind -> solarWind.setTempLevel(addLevelColor(solarWind.getIonTemperature(), upperLevels)));

        solarWinds.getSolarWindSecond().stream()
                .filter(solarWind -> solarWind.isGood())
                .forEach(solarWind -> solarWind.setTempLevel(addLevelColor(solarWind.getIonTemperature(), upperLevels)));

    }

    private int addLevelColor(Double temperature, List<Double> levels) {
        if (temperature < levels.get(0)) {
            return 1;
        } else if (temperature < levels.get(1)) {
            return 2;
        }
        else if (temperature < levels.get(2)) {
            return 3;
        }
        else if (temperature < levels.get(3)) {
            return 4;
        }
        else {
            return 5;
        }
    }

    public String getMockData() {
        return "{\n" +
                "\t\"date1\": [\n" +
                "\t{\n" +
                "\t\t\"time\": \"0001\",\n" +
                "\t\t\"bulkSpeed\": 300,\n" +
                "\t\t\"ionTemperature\": 15000.0,\n" +
                "\t\t\"isGood\": true\n" +
                "\t}, {\n" +
                "\t\t\"time\": \"0002\",\n" +
                "\t\t\"bulkSpeed\": 350,\n" +
                "\t\t\"ionTemperature\": 16000.0,\n" +
                "\t\t\"isGood\": true\n" +
                "\t}, {\n" +
                "\t\t\"time\": \"0003\",\n" +
                "\t\t\"bulkSpeed\": 800,\n" +
                "\t\t\"ionTemperature\": 1000.0,\n" +
                "\t\t\"isGood\": false\n" +
                "\t}], \n" +
                "\t\"date2\": [\n" +
                "\t{\n" +
                "\t\t\"time\": \"0001\",\n" +
                "\t\t\"bulkSpeed\": 200,\n" +
                "\t\t\"ionTemperature\": 15000.0,\n" +
                "\t\t\"isGood\": true\n" +
                "\t}, {\n" +
                "\t\t\"time\": \"0002\",\n" +
                "\t\t\"bulkSpeed\": 30,\n" +
                "\t\t\"ionTemperature\": 160000.0,\n" +
                "\t\t\"isGood\": false\n" +
                "\t}, {\n" +
                "\t\t\"time\": \"0003\",\n" +
                "\t\t\"bulkSpeed\": 100,\n" +
                "\t\t\"ionTemperature\": 3000.0,\n" +
                "\t\t\"isGood\": true\n" +
                "\t}]\n" +
                "}";
    }

}
