package com.medicconnect.medicconnect_appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PatientTimelineResponse {

    private String appointmentDate;
    private Long doctorId;
    private String diagnosis;
    private String symptoms;
    private String prescription;
    private String tests;
    private String doctorNotes;
}
