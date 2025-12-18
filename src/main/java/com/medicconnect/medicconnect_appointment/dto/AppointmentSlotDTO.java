package com.medicconnect.medicconnect_appointment.dto;

import lombok.Data;

@Data
public class AppointmentSlotDTO {
    private Long id;
    private String startTime;  // as String in HH:mm
    private String endTime;
    private String status;
    private boolean active;

    // optional: doctorSchedule info
    private Long doctorScheduleId;

}

