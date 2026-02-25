package com.medicconnect.medicconnect_appointment.controller;

import com.medicconnect.medicconnect_appointment.dto.PageResponse;
import com.medicconnect.medicconnect_appointment.dto.PatientTimelineResponse;
import com.medicconnect.medicconnect_appointment.security.AuthenticatedUser;
import com.medicconnect.medicconnect_appointment.service.PatientAppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientAppointmentService patientAppointmentService;

    @GetMapping("/{patientId}/timeline")
//    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_DOCTOR','ROLE_PATIENT')")
    public PageResponse<PatientTimelineResponse> getTimeline(
            @PathVariable Long patientId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

//            Authentication authentication) {
//        AuthenticatedUser user =
//                (AuthenticatedUser) authentication.getPrincipal();

        return patientAppointmentService.getPatientTimeline(patientId, status, page, size);
    }
}
