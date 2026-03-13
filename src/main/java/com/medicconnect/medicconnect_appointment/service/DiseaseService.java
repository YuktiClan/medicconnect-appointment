package com.medicconnect.medicconnect_appointment.service;

import com.medicconnect.medicconnect_appointment.dto.DiseaseResponseDTO;
import com.medicconnect.medicconnect_appointment.repo.DiseaseMasterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiseaseService {

    private final DiseaseMasterRepository diseaseMasterRepository;

    public DiseaseService(DiseaseMasterRepository diseaseMasterRepository) {
        this.diseaseMasterRepository = diseaseMasterRepository;
    }

    public List<DiseaseResponseDTO> getAllDiseases() {

        return diseaseMasterRepository.findByIsActiveTrue()
                .stream()
                .map(d -> new DiseaseResponseDTO(
                        d.getCode(),
                        d.getDescription()
                ))
                .toList();
    }

}
