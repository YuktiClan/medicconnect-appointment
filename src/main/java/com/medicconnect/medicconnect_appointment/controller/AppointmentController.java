package com.medicconnect.medicconnect_appointment.controller;

import com.medicconnect.medicconnect_appointment.dto.*;
import com.medicconnect.medicconnect_appointment.enums.BloodGroup;
import com.medicconnect.medicconnect_appointment.mapper.AppointmentMapper;
import com.medicconnect.medicconnect_appointment.model.Appointment;
import com.medicconnect.medicconnect_appointment.service.AppointmentService;
import com.medicconnect.medicconnect_appointment.service.AppointmentSlotService;
import com.medicconnect.medicconnect_appointment.validator.AppointmentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    @Autowired
    private AppointmentService service;
    @Autowired
    private AppointmentValidator validator;
    @Autowired
    private AppointmentSlotService appointmentService;
//    private AppointmentService appointmentService;

//    /**
//     * Create appointment (needs permission appointment.create)
//     */
//    @PostMapping
//    @PreAuthorize("hasPermission(null, 'appointment.create')")
//    public ResponseEntity<AppointmentResponseDTO> create(@RequestBody AppointmentCreateRequestDTO req,
//                                                         @RequestHeader(value = "X-User", required = false) String createdBy) {
//        AppointmentResponseDTO dto = service.createAppointment(req, createdBy);
//        return ResponseEntity.ok(dto);
//    }

//    /**
//     * Cancel appointment (permission appointment.cancel)
//     */
//    @PostMapping("/{id}/cancel")
//    @PreAuthorize("hasPermission(null, 'appointment.cancel')")
//    public ResponseEntity<?> cancel(@PathVariable Long id, @RequestHeader(value = "X-User", required = false) String cancelledBy) {
//        service.cancelAppointment(id, cancelledBy);
//        return ResponseEntity.ok().build();
//    }
//
//    /**
//     * Reschedule (permission appointment.reschedule)
//     */
//    @PostMapping("/reschedule")
//    @PreAuthorize("hasPermission(null, 'appointment.reschedule')")
//    public ResponseEntity<AppointmentFResponseDTO> reschedule(@RequestBody RescheduleRequestDTO req,
//                                                             @RequestHeader(value = "X-User", required = false) String requestedBy) {
//        AppointmentResponseDTO dto = service.reschedule(req, requestedBy);
//        return ResponseEntity.ok(dto);
//    }
//
//    @GetMapping("/patient/{orgId}/{patientId}")
//    @PreAuthorize("hasPermission(null, 'appointment.view')")
//    public ResponseEntity<List<AppointmentResponseDTO>> byPatient(@PathVariable Long orgId, @PathVariable Long patientId) {
//        return ResponseEntity.ok(service.listByPatient(orgId, patientId));
//    }
//
//    @GetMapping("/doctor/{orgId}/{doctorId}")
//    @PreAuthorize("hasPermission(null, 'appointment.view')")
//    public ResponseEntity<List<AppointmentResponseDTO>> byDoctorRange(@PathVariable Long orgId, @PathVariable Long doctorId,
//                                                                       @RequestParam(value = "from", required = false) Long fromEpoch,
//                                                                       @RequestParam(value = "to", required = false) Long toEpoch) {
//        Instant from = fromEpoch == null ? Instant.now().minusSeconds(60*60*24*30) : Instant.ofEpochMilli(fromEpoch);
//        Instant to = toEpoch == null ? Instant.now().plusSeconds(60*60*24*30) : Instant.ofEpochMilli(toEpoch);
//        return ResponseEntity.ok(service.listByDoctorAndRange(orgId, doctorId, from, to));
//    }



    @GetMapping("/blood-groups")
    public ResponseEntity<List<BloodDropdownDTO>> getBloodGroups() {


        return ResponseEntity.ok(
                Arrays.stream(BloodGroup.values())
                        .map(bg -> new BloodDropdownDTO(bg.name(), bg.getValue()))
                        .toList()
        );
    }

        @PostMapping("/{doctorId}/appointments")
        public ResponseEntity<AppointmentResponseDTO> createAppointment(
                @RequestBody CreateAppointmentRequestDTO request
        ) throws Exception {
            AppointmentResponseDTO response =
                    appointmentService.createAppointment(request);
            return ResponseEntity.ok(response);
        }

    @GetMapping("/{doctorId}/fetch")
    public ResponseEntity<List<AvailableSlotResponse>> fetchAppointments(
            @PathVariable("doctorId") Long doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(name = "status", required = false) String status
    ) {

        List<AvailableSlotResponse> response =
                appointmentService.fetchBookedAppointments(doctorId, date, status);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{appointmentId}/status")
    public ResponseEntity<Void> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @RequestParam String status
    ) {
        appointmentService.updateAppointmentStatus(appointmentId, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{appointmentId}/start")
    public ResponseEntity<?> startConsultation(
            @PathVariable Long appointmentId,
            @RequestParam("doctorId") Long doctorId
    ) {
        Appointment appointment = appointmentService.startConsultation(
                appointmentId,
                doctorId
        );

        return ResponseEntity.ok(appointment);
    }


    /* ================= GET FULL CONSULTATION ================= */

    @GetMapping("/{appointmentId}/consultation")
    public ResponseEntity<AppointmentResponseDTO> getConsultation(
            @PathVariable Long appointmentId
    ) {

        AppointmentResponseDTO dto = appointmentService.getConsultation(appointmentId);
        return ResponseEntity.ok(dto);
    }

    /* ================= ADMIN ================= */

    @PatchMapping("/{appointmentId}/admin")
    public ResponseEntity<AppointmentResponseDTO> updateAdminDetails(
            @PathVariable Long appointmentId,
            @RequestBody AdminUpdateRequest request
    ) {
        return ResponseEntity.ok(
                AppointmentMapper.toDto(appointmentService.updateAdminDetails(appointmentId, request))
        );
    }

    /* ================= DOCTOR ================= */

    @PatchMapping("/{appointmentId}/symptoms")
    public ResponseEntity<AppointmentResponseDTO> updateSymptoms(
            @PathVariable Long appointmentId,
             @RequestBody List<SymptomDTO> symptoms
    ) {
        return ResponseEntity.ok(
                AppointmentMapper.toDto(appointmentService.updateSymptoms(appointmentId, symptoms))
        );
    }

    @PatchMapping("/{appointmentId}/diagnosis")
    public ResponseEntity<Map<String, String>> updateDiagnosis(
            @PathVariable Long appointmentId,
            @RequestBody DiagnosisRequest diagnosesReq
    ) {
        appointmentService.updateDiagnosis(appointmentId, diagnosesReq);
        return ResponseEntity.ok(
                Map.of("message", "Diagnosis saved successfully")
        );
    }

    @PatchMapping("/{appointmentId}/tests")
    public ResponseEntity<AppointmentResponseDTO> updateTests(
            @PathVariable Long appointmentId,
            @RequestBody String testsJson
    ) {
        return ResponseEntity.ok(
                AppointmentMapper.toDto(appointmentService.updateTests(appointmentId, testsJson))
        );
    }

    @PatchMapping("/{appointmentId}/notes")
    public ResponseEntity<AppointmentResponseDTO> updateNotes(
            @PathVariable Long appointmentId,
            @RequestBody NotesRequest request
    ) {
        return ResponseEntity.ok(
                AppointmentMapper.toDto(appointmentService.updateNotes(appointmentId, request))
        );
    }

    /* ================= FINALIZE ================= */

    @PostMapping("/{appointmentId}/finalize")
    public ResponseEntity<Appointment> finalizeConsultation(
            @PathVariable Long appointmentId,
            @RequestParam Long doctorId
    ) {
        return ResponseEntity.ok(
                appointmentService.finalizeConsultation(appointmentId, doctorId)
        );
    }

    @GetMapping("/fetch/{appointmentId}/details")
    public ResponseEntity<AppointmentResponseDTO> fetchDiagnosis(
            @PathVariable Long appointmentId
    ) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentDetails(appointmentId)
        );
    }



}
