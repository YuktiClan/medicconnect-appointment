package com.medicconnect.medicconnect_appointment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "location_id", nullable = true)
    private Long locationId;


    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status; // BOOKED / CANCELLED

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "slot_no", nullable = false)
    private Long slotNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "consultation_start_at")
    private LocalDate consultationStartedAt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "consultation_completed_at")
    private LocalDate consultationCompletedAt;


//    @JsonFormat(pattern = "HH:mm")
//    @Column(name = "start_time", nullable = false)
//    private LocalTime startTime;
//
//    @JsonFormat(pattern = "HH:mm")
//    @Column(name = "end_time", nullable = false)
//    private LocalTime endTime;

    /* ================= ADMIN SECTION ================= */
    private String bloodPressure;
    private Integer pulse;
    private Double temperature;
    private String unit;
    private String initialComplaints;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "respiratory_rate")
    private String respiratoryRate;

    @Column(name = "spo")
    private String spo2;

    @Column(name = "weight")
    private Double weight;

    /* ================= DOCTOR SECTION ================= */
    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String prescription;

    @Column(columnDefinition = "TEXT")
    private String tests;

    @Column(columnDefinition = "TEXT")
    private String patientComments;

    @Column(columnDefinition = "TEXT")
    private String doctorNotes;

}

