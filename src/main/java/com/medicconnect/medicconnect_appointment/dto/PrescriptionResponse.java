package com.medicconnect.medicconnect_appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionResponse {
    private Long prescriptionId;
    private Long appointmentId;
    private List<CreatePrescriptionRequest.MedicineItemDto> medicines;
    private String notes;
    private LocalDate createdAt;
}

