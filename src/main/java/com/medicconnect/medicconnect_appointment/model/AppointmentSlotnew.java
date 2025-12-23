package com.medicconnect.medicconnect_appointment.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hl7.fhir.r4.model.Slot;

import java.time.LocalTime;

@Entity
@Data
@Table(name = "appointment_slot_new")
public class AppointmentSlotnew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_schedule_id", nullable = false)
    private DoctorSchedule doctorSchedule;

    private LocalTime startTime;
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private SlotStatus status; // AVAILABLE, BOOKED, BLOCKED

    private boolean active;

    // getters & setters

}

