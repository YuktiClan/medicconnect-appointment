package com.medicconnect.medicconnect_appointment.service;

import com.medicconnect.medicconnect_appointment.dto.BreakDTO;
import com.medicconnect.medicconnect_appointment.dto.DoctorBreakResponseDTO;
import com.medicconnect.medicconnect_appointment.model.DoctorBreak;
import com.medicconnect.medicconnect_appointment.model.DoctorSchedule;
import com.medicconnect.medicconnect_appointment.repo.DoctorBreakRepository;
import com.medicconnect.medicconnect_appointment.repo.DoctorScheduleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
public class DoctorBreakService {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorBreakRepository breakRepository;

    public DoctorBreakService(DoctorScheduleRepository scheduleRepository,
                              DoctorBreakRepository breakRepository) {
        this.scheduleRepository = scheduleRepository;
        this.breakRepository = breakRepository;
    }

    public DoctorBreakResponseDTO addBreak(Long doctorId, Long scheduleId, BreakDTO dto) {

        // Step 1: Validate doctor and schedule exist
        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!schedule.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Doctor does not match schedule");
        }

        // Step 2: Convert DTO to LocalTime
        LocalTime start = LocalTime.parse(dto.getStartTime());
        LocalTime end = LocalTime.parse(dto.getEndTime());

        log.info("startTime is {} and endTime is {}", start, end);

        // Step 3: Validate break is within schedule start/end
        if (start.isBefore(schedule.getStartTime()) || end.isAfter(schedule.getEndTime())) {
            throw new RuntimeException("Break time must be within schedule time");
        }

        // Step 4: Validate no overlap with existing breaks
        List<DoctorBreak> existingBreaks = breakRepository.findByDoctorScheduleId(scheduleId);
        for (DoctorBreak b : existingBreaks) {
            if (!(end.isBefore(b.getStartTime()) || start.isAfter(b.getEndTime()))) {
                throw new RuntimeException("Break overlaps existing break");
            }
        }

        // Step 5: Create and save break
        DoctorBreak doctorBreak = new DoctorBreak();
        doctorBreak.setDoctorSchedule(schedule);
        doctorBreak.setStartTime(start);
        doctorBreak.setEndTime(end);
        doctorBreak.setActive(true);

        DoctorBreak saved = breakRepository.save(doctorBreak);
        DoctorBreakResponseDTO response = new DoctorBreakResponseDTO();
        response.setId(saved.getId());
        response.setScheduleId(saved.getDoctorSchedule().getId());
        response.setStartTime(saved.getStartTime().toString());
        response.setEndTime(saved.getEndTime().toString());
        response.setActive(saved.getActive());

        return response;
    }

    public DoctorBreakResponseDTO updateBreak(Long doctorId, Long scheduleId, Long breakId,
                                              LocalTime startTime, LocalTime endTime) {

        // 1. Fetch existing break
        DoctorBreak existingBreak = breakRepository.findById(breakId)
                .orElseThrow(() -> new RuntimeException("Break not found"));

        // 2. Validate doctor ownership via schedule
        if (!existingBreak.getDoctorSchedule().getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Doctor does not own this break");
        }

        // 3. Validate schedule ID
        if (!existingBreak.getDoctorSchedule().getId().equals(scheduleId)) {
            throw new RuntimeException("Break does not belong to this schedule");
        }

        // 4. Update times
        existingBreak.setStartTime(startTime);
        existingBreak.setEndTime(endTime);

        // 5. Save updated break
        DoctorBreak savedBreak = breakRepository.save(existingBreak);

        DoctorBreakResponseDTO dto = new DoctorBreakResponseDTO();
        dto.setId(savedBreak.getId());
        dto.setStartTime(savedBreak.getStartTime().toString());
        dto.setEndTime(savedBreak.getEndTime().toString());
        dto.setActive(savedBreak.getActive());

        return dto;
    }
}

