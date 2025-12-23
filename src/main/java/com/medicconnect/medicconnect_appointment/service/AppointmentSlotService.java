package com.medicconnect.medicconnect_appointment.service;

import com.medicconnect.medicconnect_appointment.dto.*;
import com.medicconnect.medicconnect_appointment.model.*;
import com.medicconnect.medicconnect_appointment.repo.AppointmentSlotRepository;
import com.medicconnect.medicconnect_appointment.repo.DoctorBreakRepository;
import com.medicconnect.medicconnect_appointment.repo.DoctorScheduleRepository;
import com.medicconnect.medicconnect_appointment.repository.AppointmentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppointmentSlotService {

    @Autowired
    private DoctorScheduleRepository scheduleRepository;


    @Autowired
    private DoctorBreakRepository breakRepository;

    @Autowired
    private AppointmentSlotRepository slotRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Transactional
    public List<AppointmentSlot> createSlots(Long doctorId, Long scheduleId) {

        // 1. Validate schedule
        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!schedule.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Doctor mismatch");
        }

        // 2. Remove existing slots (safe re-run)
        slotRepository.deleteByDoctorScheduleId(scheduleId);

        // 3. Load active breaks
        List<DoctorBreak> breaks =
                breakRepository.findActiveBreaksByScheduleId(scheduleId);

        // 4. Generate slots
        List<AppointmentSlot> slots = new ArrayList<>();

        LocalTime current = schedule.getStartTime();
        int duration = schedule.getSlotDurationMinutes();

        while (!current.plusMinutes(duration).isAfter(schedule.getEndTime())) {

            LocalTime slotEnd = current.plusMinutes(duration);

            LocalTime finalCurrent = current;
            boolean overlapsBreak = breaks.stream().anyMatch(b ->
                    finalCurrent.isBefore(b.getEndTime()) &&
                            slotEnd.isAfter(b.getStartTime())
            );

            AppointmentSlot slot = new AppointmentSlot();
            slot.setDoctorSchedule(schedule);
            slot.setStartTime(current);
            slot.setEndTime(slotEnd);
            slot.setActive(true);
            slot.setStatus(
                    overlapsBreak ? SlotStatus.BLOCKED : SlotStatus.AVAILABLE
            );

            slots.add(slot);
            current = slotEnd;
        }

        List<AppointmentSlot> appointmentSlots = slotRepository.saveAll(slots);
        return appointmentSlots;
    }

    public AppointmentResponseDTO
    createAppointment(CreateAppointmentRequestDTO request) {

        Appointment appointment = new Appointment();
        appointment.setDoctorId(request.getDoctorId());
        appointment.setPatientId(request.getPatientId());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getEndTime());
        appointment.setStatus("BOOKED");

        Appointment save = appointmentRepository.save(appointment);

        AppointmentResponseDTO res = new AppointmentResponseDTO();
        res.setAppointmentId(save.getId());
        res.setStartTime(save.getStartTime().toString());
        return res;
    }

    public List<AppointmentSlot> getAvailableSlots(Long doctorId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        // 1. Find active schedules for doctor matching this day
        List<DoctorSchedule> schedules = scheduleRepository
                .findByDoctorIdAndActiveTrue(doctorId)
                .stream()
                .filter(s -> s.getDayOfWeek().equals(dayOfWeek))
                .collect(Collectors.toList());

        List<AppointmentSlot> availableSlots = new ArrayList<>();

        for (DoctorSchedule schedule : schedules) {

            // Check if date is within effectiveFrom and effectiveTo
            if ((schedule.getEffectiveFrom() == null || !date.isBefore(schedule.getEffectiveFrom())) &&
                    (schedule.getEffectiveTo() == null || !date.isAfter(schedule.getEffectiveTo()))) {

                LocalTime start = schedule.getStartTime();
                LocalTime end = schedule.getEndTime();
                int slotDuration = schedule.getSlotDurationMinutes();

                List<DoctorBreak> breaks = breakRepository.findActiveBreaksByScheduleId(schedule.getId());

                LocalTime current = start;
                while (!current.plusMinutes(slotDuration).isAfter(end)) {
                    LocalTime slotEnd = current.plusMinutes(slotDuration);

                    LocalTime finalCurrent = current;
                    boolean overlapsBreak = breaks.stream()
                            .anyMatch(b -> !(slotEnd.isBefore(b.getStartTime()) || finalCurrent.isAfter(b.getEndTime())));

                    if (!overlapsBreak) {
                        AppointmentSlot slot = new AppointmentSlot();
                        slot.setDoctorSchedule(schedule);
                        slot.setStartTime(current);
                        slot.setEndTime(slotEnd);
                        slot.setStatus(SlotStatus.AVAILABLE);
                        slot.setActive(true);
                        availableSlots.add(slot);
                    }

                    current = current.plusMinutes(slotDuration);
                }
            }
        }

        return availableSlots;
    }
    public AppointmentSlotDTO blockSlot(Long doctorId, Long slotId, String reason) {
        // Step 1: Get slot
        AppointmentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        // Step 2: Check if slot belongs to doctor
        if (!slot.getDoctorSchedule().getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Doctor does not own this slot");
        }

        // Step 3: Check if slot is already booked
        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new RuntimeException("Cannot block a booked slot");
        }


        // Step 4: Block the slot
        slot.setStatus(SlotStatus.BLOCKED);
        slot.setActive(true);
        // Optional: store reason if you have a field in entity
        // slot.setReason(reason);

        slotRepository.save(slot);
        AppointmentSlotDTO dto = new AppointmentSlotDTO();
        dto.setId(slot.getId());
        dto.setStartTime(slot.getStartTime().toString()); // LocalTime → String
        dto.setEndTime(slot.getEndTime().toString());
        dto.setStatus(slot.getStatus().name());
//        dto.setActive(slot.isActive());
        dto.setDoctorScheduleId(slot.getDoctorSchedule().getId());

        return dto;
    }


    public AppointmentSlotDTO unblockSlot(Long doctorId, Long slotId) {
        // Step 1: Get slot
        AppointmentSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        // Step 2: Check if slot belongs to doctor
        if (!slot.getDoctorSchedule().getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Doctor does not own this slot");
        }

        // Step 3: Check if slot is blocked (optional)
        if (slot.getStatus() != SlotStatus.BLOCKED) {
            throw new RuntimeException("Slot is not blocked");
        }

        // Step 4: Unblock the slot
        slot.setStatus(SlotStatus.AVAILABLE); // or your default status for free slots
        slot.setActive(true); // keep active true

        slotRepository.save(slot);

        // Step 5: Convert to DTO
        AppointmentSlotDTO dto = new AppointmentSlotDTO();
        dto.setId(slot.getId());
        dto.setStartTime(slot.getStartTime().toString());
        dto.setEndTime(slot.getEndTime().toString());
        dto.setStatus(slot.getStatus().name());
//        dto.setActive(slot.isActive());
        dto.setDoctorScheduleId(slot.getDoctorSchedule().getId());

        return dto;
    }
//
//    public List<AvailableSlotResponse> fetchAvailableSlots(
//            Long doctorId,
//            LocalDate date,
//            String status
//    ) {
//
//        DayOfWeek weekday = date.getDayOfWeek();
//
//        // STEP 1: Fetch weekly schedule
//        List<DoctorSchedule> schedules =
//                scheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, weekday);
//
//        if (schedules.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        // STEP 2: Fetch existing appointments for date
//
//
//        log.info("status-is--------------- {}", status);
//        List<Appointment> appointments =
//                appointmentRepository.findAppointments(doctorId, date, status);
//
//        List<AvailableSlotResponse> availableSlots = new ArrayList<>();
//
//        // STEP 3: Build slots in memory
//        for (DoctorSchedule schedule : schedules) {
//
//            LocalTime slotStart = schedule.getStartTime();
//            LocalTime slotEnd =
//                    slotStart.plusMinutes(schedule.getSlotDurationMinutes());
//
//            boolean overlaps = false;
//
//            // STEP 4: Overlap validation
//            for (Appointment appt : appointments) {
//                if (slotStart.isBefore(appt.getEndTime())
//                        && slotEnd.isAfter(appt.getStartTime())) {
//                    overlaps = true;
//                    break;
//                }
//            }
//
//            // STEP 5: Add if free
//            if (!overlaps) {
//                availableSlots.add(
//                        new AvailableSlotResponse(slotStart, slotEnd)
//                );
//            }
//        }
//
//        return availableSlots;
//    }

    public List<AvailableSlotResponse> fetchBookedAppointments(Long doctorId, LocalDate date, String status) {
        // Fetch all appointments for doctor on this date
        List<Appointment> appointments = appointmentRepository.findAppointments(doctorId, date, status);

        // Map to response DTO
        return appointments.stream()
                .map(appt -> new AvailableSlotResponse(appt.getStartTime(), appt.getEndTime(), appt.getId(), appt.getStatus()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateAppointmentStatus(Long appointmentId, String status) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus(status.toUpperCase());
        appointmentRepository.save(appointment);
    }





}

