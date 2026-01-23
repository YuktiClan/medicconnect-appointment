package com.medicconnect.medicconnect_appointment.controller;

import com.medicconnect.medicconnect_appointment.dto.ShareCalendarRequestDTO;
import com.medicconnect.medicconnect_appointment.model.DoctorGoogleToken;
import com.medicconnect.medicconnect_appointment.repo.DoctorGoogleTokenRepository;
import com.medicconnect.medicconnect_appointment.service.CalendarSharingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarSharingService calendarSharingService;
    private final DoctorGoogleTokenRepository doctorGoogleTokenRepository;

    @PostMapping("/share-with-admin/{doctorId}")
    public ResponseEntity<String> shareCalendar(
            @PathVariable Long doctorId,
            @RequestBody ShareCalendarRequestDTO dto
    ) throws Exception {

        DoctorGoogleToken token =
                doctorGoogleTokenRepository.findByDoctorId(doctorId)
                        .orElseThrow(() ->
                                new RuntimeException("Doctor has not connected Google Calendar"));

        calendarSharingService.shareCalendarWithAdmin(
                token.getRefreshToken(),
                dto.getAdminEmail()
        );

        return ResponseEntity.ok("Calendar shared successfully");
    }


}

