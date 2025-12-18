package com.medicconnect.medicconnect_appointment.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patient", indexes = {
        @Index(name = "idx_patient_org", columnList = "organization_id"),
        @Index(name = "idx_patient_abha", columnList = "abha_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // global UUID
    @Column(nullable = false, unique = true)
    private String uuid;

    // human-friendly local patient id (per org)
    @Column(name = "patient_id", nullable = false)
    private String patientId;

    // link to person entity
    @Column(name = "person_id", nullable = false)
    private Long personId;

    // ABHA (can be null)
    @Column(name = "abha_id")
    private String abhaId;

    // organization tenancy
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PatientStatus status = PatientStatus.ACTIVE;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.createdAt == null) this.createdAt = Instant.now();
    }
}
