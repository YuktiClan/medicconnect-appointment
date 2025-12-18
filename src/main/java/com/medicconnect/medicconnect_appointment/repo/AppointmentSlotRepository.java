package com.medicconnect.medicconnect_appointment.repo;

import com.medicconnect.medicconnect_appointment.model.AppointmentSlot;
import com.medicconnect.medicconnect_appointment.model.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface AppointmentSlotRepository
        extends JpaRepository<AppointmentSlot, Long> {

    void deleteByDoctorScheduleId(Long doctorScheduleId);

    List<AppointmentSlot> findByDoctorScheduleIdAndActiveTrue(Long scheduleId);

    List<AppointmentSlot> findByDoctorScheduleIdInAndStatusAndActiveTrue(
            List<Long> scheduleIds,
            SlotStatus status
    );

    List<AppointmentSlot> findByDoctorSchedule_DoctorIdAndDoctorSchedule_DateAndStatusAndActiveTrue(
            Long doctorId, LocalDate date, SlotStatus status
    );
}


