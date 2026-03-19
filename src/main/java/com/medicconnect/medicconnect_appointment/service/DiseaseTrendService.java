package com.medicconnect.medicconnect_appointment.service;

import com.medicconnect.medicconnect_appointment.dto.DiseaseSuggestionDTO;
import com.medicconnect.medicconnect_appointment.repo.DiseaseTrendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiseaseTrendService {

    @Autowired
    private DiseaseTrendRepository diseaseTrendRepository;

    public List<DiseaseSuggestionDTO> getTrendingDiseases(Long locationId) {

        return diseaseTrendRepository
                .findTop3ByLocationIdOrderByCaseCountDesc(locationId)
                .stream()
                .map(d -> new DiseaseSuggestionDTO(
                        d.getDiseaseCode(),
                        d.getDiseaseDescription()
                ))
                .toList();
    }
}
