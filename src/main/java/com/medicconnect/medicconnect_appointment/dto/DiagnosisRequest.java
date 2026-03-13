package com.medicconnect.medicconnect_appointment.dto;

import lombok.Data;

import java.util.List;

@Data
public class DiagnosisRequest {

    private List<DiagnosisDTO> diagnoses;
}
