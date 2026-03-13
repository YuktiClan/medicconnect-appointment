package com.medicconnect.medicconnect_appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiseaseResponseDTO {
    private String code;
    private String description;
}
