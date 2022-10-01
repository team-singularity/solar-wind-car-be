package com.nasa.solarwindcar.service;

import com.nasa.solarwindcar.model.SolarWind;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SolarWindService {

    public List<List<SolarWind>> fetchSolarWind(String date1, String date2) {

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
        List<List<SolarWind>> solarWind = new ArrayList<>();
        solarWind.add(solarWind1);
        solarWind.add(solarWind2);
        return solarWind;
    }

    private SolarWind parseInputToSolarWind(String input) {
        String[] row = input.split(" ");
        ArrayList<String> columns = new ArrayList<>();
        for (String word : row) {
            word = word.replaceAll("\\s", "");
            columns.add(word);
        }

        return SolarWind.builder()
                .time(columns.get(3))
                .bulkSpeed(Integer.parseInt(columns.get(8)))
                .ionTemperature(Float.parseFloat(columns.get(9)))
                .isGood(Integer.parseInt(columns.get(6)) == 0 ? true : false)
                .build();
    }

    public String getMockData() {
        return "{\n" +
                "\tdate1: [\n" +
                "\t{\n" +
                "\t\ttime: \"0001\",\n" +
                "\t\tbulkSpeed: 300,\n" +
                "\t\tionTemperature: 15000.0,\n" +
                "\t\tisGood: true\n" +
                "\t}, {\n" +
                "\t\ttime: \"0002\",\n" +
                "\t\tbulkSpeed: 350,\n" +
                "\t\tionTemperature: 16000.0,\n" +
                "\t\tisGood: true\n" +
                "\t}, {\n" +
                "\t\ttime: \"0003\",\n" +
                "\t\tbulkSpeed: 800,\n" +
                "\t\tionTemperature: 1000.0,\n" +
                "\t\tisGood: false\n" +
                "\t}], \n" +
                "\tdate2: [\n" +
                "\t{\n" +
                "\t\ttime: \"0001\",\n" +
                "\t\tbulkSpeed: 200,\n" +
                "\t\tionTemperature: 15000.0,\n" +
                "\t\tisGood: true\n" +
                "\t}, {\n" +
                "\t\ttime: \"0002\",\n" +
                "\t\tbulkSpeed: 30,\n" +
                "\t\tionTemperature: 160000.0,\n" +
                "\t\tisGood: false\n" +
                "\t}, {\n" +
                "\t\ttime: \"0003\",\n" +
                "\t\tbulkSpeed: 100,\n" +
                "\t\tionTemperature: 3000.0,\n" +
                "\t\tisGood: true\n" +
                "\t}]\n" +
                "}";
    }

}
