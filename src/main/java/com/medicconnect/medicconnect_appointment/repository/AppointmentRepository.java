package com.medicconnect.medicconnect_appointment.repository;

import com.medicconnect.medicconnect_appointment.model.Appointment;
import com.medicconnect.medicconnect_appointment.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
//    List<Appointment> findByOrganizationIdAndPatientId(Long orgId, Long patientId);
//    List<Appointment> findByOrganizationIdAndDoctorIdAndCreatedAtBetween(Long orgId, Long doctorId, Instant from, Instant to);
//    List<Appointment> findBySlotId(Long slotId);
//    boolean existsBySlotIdAndStatus(Long slotId, org.hl7.fhir.r4.model.Appointment.AppointmentStatus status);
//
//    boolean existsBySlotIdAndStatus(Long id, AppointmentStatus appointmentStatus);

    List<Appointment> findByDoctorIdAndAppointmentDate(
            Long doctorId,
            LocalDate appointmentDate
    );
}

