package com.medicconnect.medicconnect_appointment.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CreateAppointmentRequestDTO {

    private Long doctorId;
    private Long slotNo;
    private Long patientId;
    private Boolean openDayFlag;
    private String slotStartTime;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String bloodPressure;   // Example: "120/80"
    private String bloodGroup;      // Example: "O+"
    private String initialComplaints;
    private Double temperature;     // Example: 98.6
    private Double weight;          // Example: 72.5 (kg)
    private Integer pulse;          // Example: 72 (bpm)
    private Long LocationId;


}

