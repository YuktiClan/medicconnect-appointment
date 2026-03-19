package com.medicconnect.medicconnect_appointment.controller;

import com.medicconnect.medicconnect_appointment.dto.DiseaseSuggestionDTO;
import com.medicconnect.medicconnect_appointment.service.DiseaseTrendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/diseases")
public class DiseaseSuggestionController {

    @Autowired
    private DiseaseTrendService diseaseTrendService;


    @GetMapping("/trending")
    public ResponseEntity<List<DiseaseSuggestionDTO>> getTrendingDiseases(
            @RequestParam Long locationId
    ) {

        return ResponseEntity.ok(
                diseaseTrendService.getTrendingDiseases(locationId)
        );
    }
}
