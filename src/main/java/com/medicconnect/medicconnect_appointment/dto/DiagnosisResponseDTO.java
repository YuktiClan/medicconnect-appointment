package com.medicconnect.medicconnect_appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosisResponseDTO {

    private Long id;
    private String diseaseCode;
    private String diseaseDescription;
    private String diagnosisType;
    private String createdAt;
}
