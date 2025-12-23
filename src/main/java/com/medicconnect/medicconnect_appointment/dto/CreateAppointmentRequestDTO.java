package com.medicconnect.medicconnect_appointment.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CreateAppointmentRequestDTO {

    private Long doctorId;
    private Long patientId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
}

