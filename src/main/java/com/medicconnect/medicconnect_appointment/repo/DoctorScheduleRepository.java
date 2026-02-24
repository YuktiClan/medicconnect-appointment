package com.medicconnect.medicconnect_appointment.repo;


import com.medicconnect.medicconnect_appointment.model.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoctorScheduleRepository
        extends JpaRepository<DoctorSchedule, Long> {

    List<DoctorSchedule> findByDoctorIdAndActiveTrue(Long doctorId);

    Optional<DoctorSchedule> findFirstByDoctorIdAndActiveTrue(Long doctorId);

    List<DoctorSchedule> findByDoctorIdAndEffectiveFromBetween(Long doctorId, LocalDate from, LocalDate to);

    List<DoctorSchedule> findByDoctorIdAndDayOfWeek(
            Long doctorId,
            DayOfWeek weekday
    );


}
