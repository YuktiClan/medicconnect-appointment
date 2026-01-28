package com.medicconnect.medicconnect_appointment.repository;

import com.medicconnect.medicconnect_appointment.model.Appointment;
import com.medicconnect.medicconnect_appointment.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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

    @Query("""
    SELECT a
    FROM Appointment a
    WHERE a.doctorId = :doctorId
      AND a.appointmentDate = :appointmentDate
      AND (:status IS NULL OR a.status = :status)
""")
    List<Appointment> findAppointments(
            @Param("doctorId") Long doctorId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("status") AppointmentStatus status
    );

    @Query("""
    SELECT a
    FROM Appointment a
    WHERE a.doctorId = :doctorId
      AND (:status IS NULL OR a.status = :status)
""")
    List<Appointment> findAllAppointmentsByDoctorId(
            @Param("doctorId") Long doctorId,
            @Param("status") AppointmentStatus status
    );


    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusAndSlotNo(
            Long doctorId,
            LocalDate appointmentDate,
            AppointmentStatus status,
            Long slotNo
    );

    boolean existsByDoctorIdAndAppointmentDateAndSlotNo(
            Long doctorId,
            LocalDate appointmentDate,
            Integer slotNo
    );

}

