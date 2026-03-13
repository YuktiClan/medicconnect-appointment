package com.medicconnect.medicconnect_appointment.controller;

import com.medicconnect.medicconnect_appointment.dto.DiseaseResponseDTO;
import com.medicconnect.medicconnect_appointment.service.DiseaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/diseases")
public class DiseaseController {

    private final DiseaseService diseaseService;

    public DiseaseController(DiseaseService diseaseService) {
        this.diseaseService = diseaseService;
    }

    @GetMapping
    public ResponseEntity<List<DiseaseResponseDTO>> getDiseases() {

        return ResponseEntity.ok(
                diseaseService.getAllDiseases()
        );
    }
}
