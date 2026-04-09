package com.medicconnect.medicconnect_appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.medicconnect.medicconnect_appointment.model.AppointmentStatus;
import jakarta.persistence.Column;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponseDTO {


//    private Long id;
    private Long appointmentId;
//    private String uuid;
//    private Long organizationId;
    private Long doctorId;
    private Long patientId;
    private Long slotId;
    private String status;
    private String appointmentDate;

    private Long slotNo;

    private String consultationStartedAt;

    private String consultationCompletedAt;

    /* ================= ADMIN SECTION ================= */
    private String bloodPressure;
    private String bloodGroup;
    private Double weight;
    private Integer pulse;
    private Double temperature;
    private String initialComplaints;
    private String unit;
    private String respiratoryRate;
    private String spo2;

    /* ================= DOCTOR SECTION ================= */
    private String symptoms;
    private List<DiagnosisResponseDTO> diagnoses;
    private String diagnosis;
    private String prescription;
    private String tests;
    private String patientComments;
    private String doctorNotes;
}

