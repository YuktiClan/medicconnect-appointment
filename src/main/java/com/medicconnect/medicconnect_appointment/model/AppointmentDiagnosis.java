package com.medicconnect.medicconnect_appointment.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "appointment_diagnosis")
public class AppointmentDiagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "disease_id")
    private String diseaseCode;

    @Column(name = "disease_desc")
    private String diseaseDescription;

    @Column(name = "disease_type")
    private String diagnosisType;

    private LocalDateTime createdAt;

}
