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
import org.hl7.fhir.r4.model.Schedule;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
            Schedule fhirSchedule
    ) {

        List<DoctorSchedule> savedSchedules = new ArrayList<>();

        // 1️⃣ Extract start & end date from FHIR Schedule
        LocalDate startDate = fhirSchedule
                .getPlanningHorizon()
                .getStart()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate endDate = fhirSchedule
                .getPlanningHorizon()
                .getEnd()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // 2️⃣ Loop through EACH DATE in the range
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {

            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            DoctorSchedule schedule = new DoctorSchedule();
            schedule.setDoctorId(doctorId);
            schedule.setDayOfWeek(dayOfWeek);

            // ⏰ example: taken from FHIR extension or fixed for now
            schedule.setStartTime(LocalTime.of(14, 30)); // 2:30 PM
            schedule.setEndTime(LocalTime.of(18, 30));   // 6:30 PM

            schedule.setSlotDurationMinutes(15);

            // 🔑 IMPORTANT PART
            schedule.setDate(currentDate);
            schedule.setEffectiveFrom(startDate);
            schedule.setEffectiveTo(endDate);

            schedule.setActive(true);

            savedSchedules.add(scheduleRepository.save(schedule));

            currentDate = currentDate.plusDays(1);
        }

        return savedSchedules;
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


}
