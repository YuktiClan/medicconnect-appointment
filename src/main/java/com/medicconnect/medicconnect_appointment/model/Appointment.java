package com.medicconnect.medicconnect_appointment.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointment",
       indexes = {
           @Index(name = "idx_appointment_org_patient", columnList = "organization_id, patient_id"),
           @Index(name = "idx_appointment_org_doctor", columnList = "organization_id, doctor_id"),
           @Index(name = "idx_appointment_slot", columnList = "slot_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // global UUID
    @Column(nullable = false, unique = true)
    private String uuid;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(name = "reason")
    private String reason;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = Instant.now();
    }
}
