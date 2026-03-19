package com.medicconnect.medicconnect_appointment.controller;

import com.medicconnect.medicconnect_appointment.model.Location;
import com.medicconnect.medicconnect_appointment.repo.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/location")
public class LocationController {

    @Autowired
    private LocationRepository locationRepository;

    @GetMapping("/fetch")
    public ResponseEntity<List<Location>> getLocations(){

        return ResponseEntity.ok(
                locationRepository.findByIsActiveTrue()
        );
    }
}
