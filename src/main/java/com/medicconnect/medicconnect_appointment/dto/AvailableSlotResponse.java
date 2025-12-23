package com.medicconnect.medicconnect_appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalTime;

@Data
public class AvailableSlotResponse {

    private String startTime;
    private String endTime;
    private String status;
    private Long appointmentId;

    public AvailableSlotResponse(LocalTime startTime, LocalTime endTime, Long appointmentId
            , String status) {
        this.startTime = startTime.toString(); // "HH:mm"
        this.endTime = endTime.toString();
        this.appointmentId = appointmentId;
        this.status = status;
    }
}

