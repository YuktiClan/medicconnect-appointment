package com.medicconnect.medicconnect_appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePrescriptionRequest {

    private Long appointmentId;
    private Long doctorId;
    private String notes;
    private Integer timesPerDay;    // dynamic n
    private List<MedicineItemDto> medicines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicineItemDto {
        private String name;
        private String dosage;
        private Integer frequency;
        private Integer duration;
        private Integer time;        // meal time code (1–5)

    }
}

