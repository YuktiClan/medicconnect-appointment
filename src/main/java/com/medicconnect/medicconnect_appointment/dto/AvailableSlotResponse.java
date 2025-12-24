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
    private Long slotNo;

    public AvailableSlotResponse(Long appointmentId
            , String status, Long slotNo) {
        this.appointmentId = appointmentId;
        this.status = status;
        this.slotNo = slotNo;
    }
}

