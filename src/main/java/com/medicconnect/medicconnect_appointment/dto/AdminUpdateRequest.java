package com.medicconnect.medicconnect_appointment.dto;

import lombok.Data;

@Data
public class AdminUpdateRequest {
    private String bloodPressure;
    private Integer pulse;
    private Double temperature;
    private String initialComplaints;
}

