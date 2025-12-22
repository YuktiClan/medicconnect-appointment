package com.medicconnect.medicconnect_appointment.service;


import com.medicconnect.medicconnect_appointment.dto.BreakResponseDTO;
import com.medicconnect.medicconnect_appointment.dto.DoctorScheduleViewDTO;
import com.medicconnect.medicconnect_appointment.dto.SlotResponseDTO;
import com.medicconnect.medicconnect_appointment.mapper.DoctorScheduleMapper;
import com.medicconnect.medicconnect_appointment.model.AppointmentSlot;
import com.medicconnect.medicconnect_appointment.model.DoctorBreak;
import com.medicconnect.medicconnect_appointment.model.DoctorSchedule;
import com.medicconnect.medicconnect_appointment.repo.AppointmentSlotRepository;
import com.medicconnect.medicconnect_appointment.repo.DoctorBreakRepository;
import com.medicconnect.medicconnect_appointment.repo.DoctorScheduleRepository;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Schedule;
import org.hl7.fhir.r4.model.codesystems.DaysOfWeek;
import org.hl7.fhir.r4.model.Enumeration;

import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorBreakRepository breakRepository;
    private final AppointmentSlotRepository slotRepository;

    public DoctorScheduleService(DoctorScheduleRepository scheduleRepository, DoctorBreakRepository breakRepository, AppointmentSlotRepository slotRepository) {
        this.scheduleRepository = scheduleRepository;
        this.breakRepository = breakRepository;
        this.slotRepository = slotRepository;
    }

    public List<DoctorSchedule> createSchedule(
            Long doctorId,
            PractitionerRole role
    ) {

        List<DoctorSchedule> savedSchedules = new ArrayList<>();

        // Read slot duration (custom FHIR extension)
        Integer slotDuration =
                role.getExtension().stream()
                        .filter(ext ->
                                ext.getUrl().equals(
                                        "http://medicconnect.com/fhir/StructureDefinition/slot-duration"
                                )
                        )
                        .findFirst()
                        .map(ext -> ext.getValue().primitiveValue())
                        .map(Integer::valueOf)
                        .orElseThrow(() ->
                                new RuntimeException("Slot duration missing")
                        );

        // Iterate availableTime blocks
        for (PractitionerRole.PractitionerRoleAvailableTimeComponent at
                : role.getAvailableTime()) {

            // Each availableTime can have multiple days
            at.getDaysOfWeek().forEach(dayEnum -> {

                DayOfWeek day =
                        mapFhirDayToJava(dayEnum.getValue().name());

                DoctorSchedule entity = new DoctorSchedule();
                entity.setDoctorId(doctorId);
                entity.setDayOfWeek(day);
                entity.setStartTime(
                        LocalTime.parse(at.getAvailableStartTime())
                );
                entity.setEndTime(
                        LocalTime.parse(at.getAvailableEndTime())
                );
                entity.setSlotDurationMinutes(slotDuration);

                // Infinite schedule
                entity.setEffectiveFrom(LocalDate.now());
                entity.setEffectiveTo(null);

                entity.setActive(true);

                savedSchedules.add(
                        scheduleRepository.save(entity)
                );
            });
        }

        return savedSchedules;
    }



    private DayOfWeek mapFhirDayToJava(String fhirDay) {
        return switch (fhirDay.toLowerCase()) {
            case "mon" -> DayOfWeek.MONDAY;
            case "tue" -> DayOfWeek.TUESDAY;
            case "wed" -> DayOfWeek.WEDNESDAY;
            case "thu" -> DayOfWeek.THURSDAY;
            case "fri" -> DayOfWeek.FRIDAY;
            case "sat" -> DayOfWeek.SATURDAY;
            case "sun" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Invalid day: " + fhirDay);
        };
    }

    public DoctorScheduleViewDTO viewDoctorSchedule(Long doctorId) {

        // Step 1: Get active schedule
        DoctorSchedule schedule = scheduleRepository
                .findFirstByDoctorIdAndActiveTrue(doctorId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        // Step 2: Get breaks
        List<DoctorBreak> breaks =
                breakRepository.findByDoctorScheduleIdAndActiveTrue(schedule.getId());

        // Step 3: Get slots
        List<AppointmentSlot> slots =
                slotRepository.findByDoctorScheduleIdAndActiveTrue(schedule.getId());

        // Step 4: Map response
        DoctorScheduleViewDTO response = new DoctorScheduleViewDTO();
        response.setStartTime(schedule.getStartTime().toString());
        response.setEndTime(schedule.getEndTime().toString());

        // Map breaks
        List<BreakResponseDTO> breakDTOs = breaks.stream().map(b -> {
            BreakResponseDTO dto = new BreakResponseDTO();
            dto.setStartTime(b.getStartTime().toString());
            dto.setEndTime(b.getEndTime().toString());
            return dto;
        }).toList();

        // Map slots
        List<SlotResponseDTO> slotDTOs = slots.stream().map(s -> {
            SlotResponseDTO dto = new SlotResponseDTO();
            dto.setStartTime(s.getStartTime().toString());
            dto.setEndTime(s.getEndTime().toString());
            dto.setStatus(s.getStatus().name());
            return dto;
        }).toList();

        response.setBreaks(breakDTOs);
        response.setSlots(slotDTOs);

        return response;
    }

    public DoctorSchedule updateSchedule(Long doctorId, Long scheduleId, Schedule fhirSchedule) {
        // Fetch existing schedule
        DoctorSchedule existing = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        // Optional: Verify doctor owns this schedule
        if (!existing.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Doctor does not own this schedule");
        }
        Date startDate = fhirSchedule.getPlanningHorizon().getStart();
        Date endDate = fhirSchedule.getPlanningHorizon().getEnd();

        LocalTime startTime = Instant.ofEpochMilli(startDate.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalTime();

        LocalTime endTime = Instant.ofEpochMilli(endDate.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalTime();
        // Map FHIR Schedule fields to your entity
        existing.setStartTime(startTime);
        existing.setEndTime(endTime);
        Date effectiveStart = fhirSchedule.getPlanningHorizon().getStart();
        Date effectiveEnd = fhirSchedule.getPlanningHorizon().getEnd();

        existing.setEffectiveFrom(Instant.ofEpochMilli(effectiveStart.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate());

        if (effectiveEnd != null) {
            existing.setEffectiveTo(Instant.ofEpochMilli(effectiveEnd.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate());
        }
        return scheduleRepository.save(existing);
    }


    public int deleteSchedulesInRange(Long doctorId, LocalDate fromDate, LocalDate toDate) {
        List<DoctorSchedule> schedules = scheduleRepository.findByDoctorIdAndEffectiveFromBetween(
                doctorId, fromDate, toDate);

        if (schedules.isEmpty()) {
            throw new RuntimeException("No schedules found in the specified range");
        }

        scheduleRepository.deleteAll(schedules);

        return schedules.size();
    }

}
