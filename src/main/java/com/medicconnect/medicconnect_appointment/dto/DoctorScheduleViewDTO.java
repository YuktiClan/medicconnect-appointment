package com.medicconnect.medicconnect_appointment.dto;

import lombok.Data;

import java.util.List;

@Data
public class DoctorScheduleViewDTO {

    private String startTime; // 12:00
    private String endTime;   // 14:00

    private List<BreakResponseDTO> breaks;
    private List<SlotResponseDTO> slots;

}

