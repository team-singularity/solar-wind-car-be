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

    @PostMapping(path="/data",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSolarWindByDate(@PathVariable String date1, @PathVariable String date2) {
        return ResponseEntity.ok(service.fetchSolarWind(date1, date2));
    }

    @GetMapping(path="/mock-data")
    public ResponseEntity<?> getMockData(@RequestParam String date1, @RequestParam String date2) {
        return ResponseEntity.ok(service.getMockData());
    }

}
