package com.medicconnect.medicconnect_appointment.dto;

import lombok.Data;

@Data
public class DoctorBreakResponseDTO {

    private Long id;
    private Long scheduleId;
    private String startTime;
    private String endTime;
    private boolean active;

    // getters & setters
}

