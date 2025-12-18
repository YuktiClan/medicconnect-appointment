package com.medicconnect.medicconnect_appointment.dto;

import lombok.Data;

@Data
public class SlotResponseDTO {

    private String startTime; // 12:00
    private String endTime;   // 12:30
    private String status;    // AVAILABLE / BOOKED / BREAK

}

