package com.nasa.solarwindcar.controller;

import com.nasa.solarwindcar.service.SolarWindService;
import com.nasa.solarwindcar.service.SolarWindServiceNASAImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class SolarWindController {

    private final SolarWindService service;

    public SolarWindController(@Qualifier("NOAA") SolarWindService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String sentHelloWorld() {
        return "Hello world!";
    }

    @GetMapping(path="/data")
    @CrossOrigin("*")
    public ResponseEntity<?> getSolarWindByDate(@RequestParam String date1, @RequestParam String date2) {
        return ResponseEntity.ok(service.fetchSolarWind(date1, date2));
    }

}
