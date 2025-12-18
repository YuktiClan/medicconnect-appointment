package com.medicconnect.medicconnect_appointment.repository;

import com.medicconnect.medicconnect_appointment.model.AppointmentSlotm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AppointmentSlotRepositorym extends JpaRepository<AppointmentSlotm, Long> {

    List<AppointmentSlotm> findByOrganizationIdAndDoctorIdAndStartTimeBetween(Long orgId, Long doctorId, Instant from, Instant to);

    // PESSIMISTIC lock when reserving to avoid race conditions
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("select s from com.medicconnect.appointment.model.AppointmentSlot s where s.id = :id")
    Optional<AppointmentSlotm> findById(@Param("id") Long id);
//    Optional<AppointmentSlot> findByIdForUpdate(@Param("id") Long id);

    List<AppointmentSlotm> findByOrganizationIdAndStartTimeBetween(Long orgId, Instant from, Instant to);

    List<AppointmentSlotm> findByOrganizationIdAndDoctorId(Long orgId, Long doctorId);

}
