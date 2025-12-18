package com.medicconnect.medicconnect_appointment.mapper;


import com.medicconnect.medicconnect_appointment.model.DoctorSchedule;
import org.hl7.fhir.r4.model.Schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class DoctorScheduleMapper {

    public static DoctorSchedule toEntity(
            Schedule fhirSchedule,
            Long doctorId,
            DayOfWeek dayOfWeek
    ) {

        DoctorSchedule entity = new DoctorSchedule();

        entity.setDoctorId(doctorId);
        entity.setDayOfWeek(dayOfWeek);

        // Using FHIR PlanningHorizon
        entity.setStartTime(
                LocalTime.from(
                        fhirSchedule.getPlanningHorizon().getStart().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                )
        );

        entity.setEndTime(
                LocalTime.from(
                        fhirSchedule.getPlanningHorizon().getEnd().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                )
        );

        entity.setSlotDurationMinutes(15); // fixed for now
        entity.setEffectiveFrom(LocalDate.now());
        entity.setActive(true);

        return entity;
    }
}

