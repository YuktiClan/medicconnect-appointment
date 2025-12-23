package com.medicconnect.medicconnect_appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalTime;

@Data
public class AvailableSlotResponse {

    private String startTime;
    private String endTime;

    public AvailableSlotResponse(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime.toString(); // "HH:mm"
        this.endTime = endTime.toString();
    }
}

