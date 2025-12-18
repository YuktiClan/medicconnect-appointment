package com.medicconnect.medicconnect_appointment.repository;

import com.medicconnect.medicconnect_appointment.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByOrganizationIdAndPatientId(Long orgId, Long patientId);
    List<Appointment> findByOrganizationIdAndDoctorIdAndCreatedAtBetween(Long orgId, Long doctorId, Instant from, Instant to);
    List<Appointment> findBySlotId(Long slotId);
}
