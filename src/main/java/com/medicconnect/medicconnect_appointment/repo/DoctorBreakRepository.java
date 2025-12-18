package com.medicconnect.medicconnect_appointment.repo;


import com.medicconnect.medicconnect_appointment.model.DoctorBreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorBreakRepository extends JpaRepository<DoctorBreak, Long> {
    List<DoctorBreak> findByDoctorScheduleId(Long scheduleId);

    @Query("""
        SELECT b FROM DoctorBreak b
        WHERE b.doctorSchedule.id = :scheduleId
        AND b.active = true
    """)
    List<DoctorBreak> findActiveBreaksByScheduleId(
            @Param("scheduleId") Long scheduleId
    );

    List<DoctorBreak> findByDoctorScheduleIdAndActiveTrue(Long scheduleId);

}


