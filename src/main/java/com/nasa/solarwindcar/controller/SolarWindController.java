package com.nasa.solarwindcar.controller;

import com.nasa.solarwindcar.service.SolarWindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SolarWindController {

    @Autowired
    private SolarWindService service;

    @GetMapping("/")
    public String sentHelloWorld() {
        return "Hello world!";
    }

    @GetMapping(path="/data")
    @CrossOrigin("*")
    public ResponseEntity<?> getSolarWindByDate(@RequestParam String date1, @RequestParam String date2) {
        return ResponseEntity.ok(service.fetchSolarWind(date1, date2));
    }

    @GetMapping(path="/mock-data")
    public ResponseEntity<?> getMockData(@RequestParam String date1, @RequestParam String date2) {
        return ResponseEntity.ok(service.getMockData());
    }

}
