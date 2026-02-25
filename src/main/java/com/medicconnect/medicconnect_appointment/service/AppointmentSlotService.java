package com.medicconnect.medicconnect_appointment.service;

import com.medicconnect.medicconnect_appointment.dto.*;
import com.medicconnect.medicconnect_appointment.mapper.AppointmentMapper;
import com.medicconnect.medicconnect_appointment.model.*;
import com.medicconnect.medicconnect_appointment.repo.AppointmentSlotRepository;
import com.medicconnect.medicconnect_appointment.repo.DoctorBreakRepository;
import com.medicconnect.medicconnect_appointment.repo.DoctorScheduleRepository;
import com.medicconnect.medicconnect_appointment.repository.AppointmentRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
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

    @Autowired
    private AppointmentRedisLockService redisLockService;

    @Autowired
    private DoctorGoogleAvailabilityService doctorGoogleAvailabilityService;

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

    public AppointmentResponseDTO createAppointment(CreateAppointmentRequestDTO request) throws Exception {

        Appointment appointment = new Appointment();
        appointment.setDoctorId(request.getDoctorId());
        appointment.setPatientId(request.getPatientId());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStatus(AppointmentStatus.valueOf("PENDING"));
        appointment.setBloodPressure(Objects.nonNull(request.getBloodPressure()) ? request.getBloodPressure() : "");
        appointment.setPulse(Objects.nonNull(request.getPulse()) ? request.getPulse() : 0);
        appointment.setTemperature(Objects.nonNull(request.getTemperature()) ? request.getTemperature() : 0);
        appointment.setInitialComplaints(Objects.nonNull(request.getInitialComplaints()) ? request.getInitialComplaints() : "");
        appointment.setBloodGroup(Objects.nonNull(request.getBloodGroup()) ? request.getBloodGroup() : "");
        appointment.setWeight(Objects.nonNull(request.getWeight()) ? request.getWeight() : 0);

        int slotDuration = 15;
        LocalTime selectedStart = null;
        boolean openDayFlag = Boolean.TRUE.equals(request.getOpenDayFlag());
        /*
         *  system generated slot no as per its doctors schedule
         * */
//        Long slotNo;

        if (!openDayFlag) {
            // UI is sending slot number directly → validate & save as is
            if (request.getSlotNo() == null) {
                throw new RuntimeException("slotNo is required when openDayFlag = false");
            }
//            slotNo = request.getSlotNo();
            List<Appointment> appointments = appointmentRepository
                    .findByDoctorIdAndAppointmentDateAndStatusAndSlotNo(
                            request.getDoctorId(),
                            request.getAppointmentDate(),
                            AppointmentStatus.valueOf("PENDING"),
                            request.getSlotNo()
                    );

            if (!appointments.isEmpty()) {
                throw new RuntimeException("Appointment already exists for slotNo " + request.getSlotNo());
            }

            appointment.setSlotNo(request.getSlotNo());
        }
        else {
            List<DoctorSchedule> schedules = scheduleRepository
                    .findByDoctorIdAndDayOfWeek(request.getDoctorId(), request.getAppointmentDate().getDayOfWeek());

            if (schedules.isEmpty()) {
                throw new RuntimeException("Doctor has no schedule for this weekday");
            }

           slotDuration = schedules.get(0).getSlotDurationMinutes();
            Integer generatedSlotNo = null;
            LocalTime now = LocalTime.now();

            schedules.sort(Comparator.comparing(DoctorSchedule::getStartTime));
            int globalSlotCounter = 0;

            for (DoctorSchedule sch : schedules) {
                LocalTime pointer = sch.getStartTime();
                LocalTime end = sch.getEndTime();

                while (!pointer.isAfter(end.minusMinutes(slotDuration))) {
                    globalSlotCounter++;

                    boolean booked = appointmentRepository
                            .existsByDoctorIdAndAppointmentDateAndSlotNo(
                                    request.getDoctorId(),
                                    request.getAppointmentDate(),
                                    globalSlotCounter
                            );

                    if (pointer.isAfter(now) && !booked) {
                        selectedStart = pointer;
                        generatedSlotNo = globalSlotCounter;
                        break;
                    }

                    pointer = pointer.plusMinutes(slotDuration);
                }

                if (selectedStart != null) break;
            }

            if (generatedSlotNo == null) {
                throw new RuntimeException(" No available future slots remaining for today");
            }

            appointment.setSlotNo((long) generatedSlotNo);
        }
        String redisKey = redisLockService.buildKey(
                request.getDoctorId(),
                request.getAppointmentDate(),
                appointment.getSlotNo()
        );

        boolean lockAcquired = redisLockService.tryLock(redisKey);

        if (!lockAcquired) {
            throw new RuntimeException("Appointment request already in progress for this slot");
        }

        // ===== GOOGLE CALENDAR AVAILABILITY CHECK =====
        doctorGoogleAvailabilityService
                .validateDoctorAvailabilityOnGoogle(
                        request.getDoctorId(),
                        request.getAppointmentDate(),
                        LocalTime.parse(request.getSlotStartTime()),
                        slotDuration
                );

        doctorGoogleAvailabilityService
                .createDoctorEventOnGoogle(
                        request.getDoctorId(),
                        request.getAppointmentDate(),
                        LocalTime.parse(request.getSlotStartTime()),
                        slotDuration
        );
        // ============================================

        try {
//        redisLockService.markBooked(redisKey);
        Appointment save = appointmentRepository.save(appointment);

        AppointmentResponseDTO res = new AppointmentResponseDTO();
        res.setAppointmentId(save.getId());
        res.setSlotId(request.getSlotNo());
        res.setDoctorId(request.getDoctorId());
        res.setPatientId(request.getPatientId());
        res.setStatus(save.getStatus().name());
        res.setSlotId(save.getSlotNo());
        return res;
        } catch (Exception ex) {
//            redisLockService.release(redisKey);
            throw ex;
        }
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

//    public List<AvailableSlotResponse> fetchBookedAppointments(Long doctorId, LocalDate date, String status) {
//        // Fetch all appointments for doctor on this date
//        AppointmentStatus appointmentStatus = Objects.nonNull(status) && !status.isEmpty() ? AppointmentStatus.valueOf(status) : null;
//        List<Appointment> appointments;
//        if (Objects.isNull(date)){
//            appointments = appointmentRepository.findAllAppointmentsByDoctorId(doctorId, appointmentStatus);
//        }else {
//            appointments = appointmentRepository.findAppointments(doctorId, date, appointmentStatus);
//        }
//        log.info("fetching records for doctorId - {} , date- {}, appointmentstatus - {} and records size is - {}", doctorId, date, appointmentStatus, appointments.size() );
//
//        // Map to response DTO
//        return appointments.stream()
//                .map(appt -> new AvailableSlotResponse( appt.getId(), appt.getStatus().name(), appt.getSlotNo(), appt.getPatientId(), appt.getDoctorId()
//                ))
//                .collect(Collectors.toList());
//    }

    public List<AvailableSlotResponse> fetchBookedAppointments(
            Long doctorId,
            LocalDate date,
            String status) {

        AppointmentStatus appointmentStatus =
                Objects.nonNull(status) && !status.isEmpty()
                        ? AppointmentStatus.valueOf(status)
                        : null;

        List<Appointment> appointments;

        if (Objects.isNull(date)) {
            appointments = appointmentRepository
                    .findAllAppointmentsByDoctorId(doctorId, appointmentStatus);
        } else {
            appointments = appointmentRepository
                    .findAppointments(doctorId, date, appointmentStatus);
        }

        log.info("fetching records for doctorId - {} , date- {}, appointmentstatus - {} and records size is - {}",
                doctorId, date, appointmentStatus, appointments.size());

        //  Get locked slots from Redis
        Set<Long> lockedSlots = Objects.nonNull(date)
                ? redisLockService.getLockedSlots(doctorId, date)
                : Set.of();

        // Convert DB appointments
        List<AvailableSlotResponse> response =
                appointments.stream()
                        .map(appt ->
                                new AvailableSlotResponse(
                                        appt.getId(),
                                        appt.getStatus().name(),
                                        appt.getSlotNo(),
                                        appt.getPatientId(),
                                        appt.getDoctorId()
                                ))
                        .collect(Collectors.toList());

        //  Add locked slots (not yet saved in DB)
        for (Long lockedSlot : lockedSlots) {

            boolean alreadyInDb = response.stream()
                    .anyMatch(r -> r.getSlotNo().equals(lockedSlot));

            if (!alreadyInDb) {
                response.add(new AvailableSlotResponse(
                        null,
                        "IN_PROGRESS",
                        lockedSlot,
                        null,
                        doctorId
                ));
            }
        }

        return response;
    }

    @Transactional
    public void updateAppointmentStatus(Long appointmentId, String status) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        AppointmentStatus appointmentStatus;
        try {
            appointmentStatus = AppointmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(
                    "Invalid appointment status: " + status
            );
        }
        if (appointment.getStatus().name().equalsIgnoreCase(appointmentStatus.name())){
            throw new RuntimeException("Appointment is already updated with status-" +  status);
        }
        appointment.setStatus(appointmentStatus);
        appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment startConsultation(Long appointmentId, Long doctorId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        //Validate doctor ownership
        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Doctor not authorized for this appointment");
        }

        //Validate appointment status
        if (appointment.getStatus() == AppointmentStatus.CHECKED_IN) {
            throw new RuntimeException("Consultation already started");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Consultation already completed");
        }
//
//        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
//            throw new RuntimeException("Invalid appointment state");
//        }

        // Start consultation
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointment.setConsultationStartedAt(LocalDate.now());

        return appointmentRepository.save(appointment);
    }

    /* ================= GET ================= */

    public AppointmentResponseDTO getConsultation(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        return AppointmentMapper.toDto(appointment);
    }

    /* ================= ADMIN ================= */

    public Appointment updateAdminDetails(Long appointmentId, AdminUpdateRequest request) {

        Appointment appt = getAppointmentForEdit(appointmentId);

        appt.setBloodPressure(request.getBloodPressure());
        appt.setPulse(request.getPulse());
        appt.setTemperature(request.getTemperature());
        appt.setInitialComplaints(request.getInitialComplaints());

        return appointmentRepository.save(appt);
    }

    /* ================= DOCTOR ================= */

    public Appointment updateSymptoms(Long appointmentId, String symptomsJson) {
        Appointment appt = getAppointmentForEdit(appointmentId);
        appt.setSymptoms(symptomsJson);
        return appointmentRepository.save(appt);
    }

    public Appointment updateDiagnosis(Long appointmentId, String diagnosisJson) {
        Appointment appt = getAppointmentForEdit(appointmentId);
        appt.setDiagnosis(diagnosisJson);
        return appointmentRepository.save(appt);
    }

    public Appointment updateTests(Long appointmentId, String testsJson) {
        Appointment appt = getAppointmentForEdit(appointmentId);
        appt.setTests(testsJson);
        return appointmentRepository.save(appt);
    }

    public Appointment updateNotes(Long appointmentId, NotesRequest request) {
        Appointment appt = getAppointmentForEdit(appointmentId);
        appt.setPatientComments(request.getPatientComments());
        appt.setDoctorNotes(request.getDoctorNotes());
        return appointmentRepository.save(appt);
    }

    /* ================= FINALIZE ================= */

    public Appointment finalizeConsultation(Long appointmentId, Long doctorId) {

        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appt.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Doctor not authorized");
        }

        if (appt.getStatus() != AppointmentStatus.CHECKED_IN) {
            throw new RuntimeException("Consultation not active");
        }

        appt.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appt);
    }

    /* ================= COMMON ================= */

    private Appointment getAppointmentForEdit(Long appointmentId) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appt.getStatus() != AppointmentStatus.CHECKED_IN) {
            throw new RuntimeException("Consultation not editable");
        }
        return appt;
    }

    public AppointmentResponseDTO getAppointmentDetails(Long appointmentId) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        return AppointmentMapper.toDto(appt);
    }


}

