package com.medicconnect.medicconnect_appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalTime;

@Data
public class AvailableSlotResponse {

    private String status;
    private Long appointmentId;
    private Long slotNo;
    private Long patientId;
    private Long doctorId;

    public AvailableSlotResponse(Long appointmentId
            , String status, Long slotNo, Long patientId, Long doctorId) {
        this.appointmentId = appointmentId;
        this.status = status;
        this.slotNo = slotNo;
        this.patientId = patientId;
        this.doctorId = doctorId;
    }
}

