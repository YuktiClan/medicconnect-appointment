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
import org.hl7.fhir.r4.model.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

import java.time.LocalDate;
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

    @PostMapping(path = "/{doctorId}/schedule", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<DoctorSchedule>> createDoctorSchedule(
            @PathVariable Long doctorId,
            @RequestBody String scheduleJson // receive as raw JSON string
    ) {
        // Parse JSON into FHIR Schedule POJO
        IParser parser = fhirContext.newJsonParser();
        Schedule fhirSchedule = parser.parseResource(Schedule.class, scheduleJson);

        List<DoctorSchedule> schedules = scheduleService.createSchedule(doctorId, fhirSchedule);
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

}
