package com.medicconnect.medicconnect_appointment.controller;

import com.medicconnect.medicconnect_appointment.dto.AppointmentSlotDTO;
import com.medicconnect.medicconnect_appointment.dto.BreakDTO;
import com.medicconnect.medicconnect_appointment.dto.DoctorBreakResponseDTO;
import com.medicconnect.medicconnect_appointment.dto.DoctorScheduleViewDTO;
import com.medicconnect.medicconnect_appointment.model.AppointmentSlot;
import com.medicconnect.medicconnect_appointment.model.DoctorSchedule;
import com.medicconnect.medicconnect_appointment.service.AppointmentSlotService;
import com.medicconnect.medicconnect_appointment.service.DoctorBreakService;
import com.medicconnect.medicconnect_appointment.service.DoctorScheduleService;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctors")
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;
    private final DoctorBreakService breakService;
    private final FhirContext fhirContext = FhirContext.forR4(); // HAPI FHIR R4


    @Autowired
    private AppointmentSlotService slotService;

    public DoctorScheduleController(DoctorScheduleService scheduleService, DoctorBreakService breakService) {
        this.scheduleService = scheduleService;
        this.breakService = breakService;
    }


    @PostMapping(
            path = "/{doctorId}/schedule",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<List<DoctorSchedule>> createDoctorSchedule(
            @PathVariable Long doctorId,
            @RequestBody String practitionerRoleJson
    ) {

        IParser parser = fhirContext.newJsonParser();

        PractitionerRole role =
                parser.parseResource(PractitionerRole.class, practitionerRoleJson);

        List<DoctorSchedule> schedules =
                scheduleService.createSchedule(doctorId, role);

        return ResponseEntity.ok(schedules);
    }



    @PostMapping("/{doctorId}/schedule/{scheduleId}/breaks")
    public ResponseEntity<DoctorBreakResponseDTO> addBreak(
            @PathVariable Long doctorId,
            @PathVariable Long scheduleId,
            @RequestBody BreakDTO breakDTO
    ) {
        // Step 1: Validate and create break
        DoctorBreakResponseDTO createdBreak = breakService.addBreak(doctorId, scheduleId, breakDTO);
        return ResponseEntity.ok(createdBreak);
    }


    @PostMapping("/{doctorId}/schedules/{scheduleId}/slots")
    public ResponseEntity<List<AppointmentSlot>> createSlots(
            @PathVariable Long doctorId,
            @PathVariable Long scheduleId
    ) {
        slotService.createSlots(doctorId, scheduleId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(
            path = "/{doctorId}/schedule",
            produces = "application/json"
    )
    public ResponseEntity<DoctorScheduleViewDTO> viewOwnSchedule(
            @PathVariable Long doctorId
    ) {
        DoctorScheduleViewDTO response =
                scheduleService.viewDoctorSchedule(doctorId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{doctorId}/slots")
    public ResponseEntity<List<AppointmentSlot>> getSlots(
            @PathVariable Long doctorId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<AppointmentSlot> slots = slotService.getAvailableSlots(doctorId, date);
        return ResponseEntity.ok(slots);
    }

    @PostMapping("/{doctorId}/slots/{slotId}/block")
    public ResponseEntity<AppointmentSlotDTO> blockSlot(
            @PathVariable Long doctorId,
            @PathVariable Long slotId,
            @RequestBody(required = false) Map<String, String> reasonMap) {

        String reason = reasonMap != null ? reasonMap.get("reason") : null;
        AppointmentSlotDTO blockedSlot = slotService.blockSlot(doctorId, slotId, reason);
        return ResponseEntity.ok(blockedSlot);
    }

    @PutMapping(path = "/{doctorId}/schedule/{scheduleId}",
            consumes = "application/json", produces = "application/json")
    public ResponseEntity<DoctorSchedule> updateDoctorSchedule(
            @PathVariable Long doctorId,
            @PathVariable Long scheduleId,
            @RequestBody String scheduleJson // raw FHIR JSON
    ) {
        // 1. Parse JSON into FHIR Schedule POJO
        IParser parser = fhirContext.newJsonParser();
        Schedule fhirSchedule = parser.parseResource(Schedule.class, scheduleJson);

        // 2. Call service to update schedule
        DoctorSchedule updatedSchedule = scheduleService.updateSchedule(doctorId, scheduleId, fhirSchedule);

        // 3. Return updated schedule
        return ResponseEntity.ok(updatedSchedule);
    }

    @PutMapping("/{doctorId}/schedule/{scheduleId}/breaks/{breakId}")
    public ResponseEntity<DoctorBreakResponseDTO> updateBreak(
            @PathVariable Long doctorId,
            @PathVariable Long scheduleId,
            @PathVariable Long breakId,
            @RequestBody BreakDTO breakDTO
    ) {
        // Convert string times ("HH:mm:ss") to LocalTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime startTime = LocalTime.parse(breakDTO.getStartTime(), formatter);
        LocalTime endTime = LocalTime.parse(breakDTO.getEndTime(), formatter);

        // Call service to update break
        DoctorBreakResponseDTO updatedBreak = breakService.updateBreak(
                doctorId, scheduleId, breakId, startTime, endTime);

        return ResponseEntity.ok(updatedBreak);
    }

    @PutMapping("/{doctorId}/slots/{slotId}/unblock")
    public ResponseEntity<AppointmentSlotDTO> unblockSlot(
            @PathVariable Long doctorId,
            @PathVariable Long slotId
    ) {
        AppointmentSlotDTO dto = slotService.unblockSlot(doctorId, slotId);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{doctorId}/schedule")
    public ResponseEntity<String> deleteDoctorSchedule(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        // Call service to delete schedules in the date range
        int deletedCount = scheduleService.deleteSchedulesInRange(doctorId, fromDate, toDate);

        return ResponseEntity.ok(deletedCount + " schedule(s) deleted successfully");


    }


}
