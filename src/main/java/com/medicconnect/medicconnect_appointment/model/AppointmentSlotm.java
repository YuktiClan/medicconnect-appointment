package com.medicconnect.medicconnect_appointment.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "appointment_slot_m")
//        ,
//       indexes = {
//           @Index(name = "idx_slot_org_doctor_start", columnList = "organization_id, doctor_id, start_time")
//       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentSlotm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    /**
     * doctorId refers to Person.id for the clinician
     */
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    /**
     * number of simultaneous appointments allowed in this slot (capacity)
     */
    @Column(name = "capacity", nullable = false)
    private Integer capacity = 1;

    /**
     * current reserved count - maintained transactionally
     */
    @Column(name = "reserved_count", nullable = false)
    private Integer reservedCount = 0;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void beforePersist() {
        if (this.createdAt == null) this.createdAt = Instant.now();
        if (this.reservedCount == null) this.reservedCount = 0;
        if (this.capacity == null) this.capacity = 1;
    }

    public boolean hasCapacity() {
        return reservedCount < capacity;
    }
}
