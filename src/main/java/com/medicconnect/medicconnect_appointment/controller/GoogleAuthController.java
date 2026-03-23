package com.medicconnect.medicconnect_appointment.controller;

import com.medicconnect.medicconnect_appointment.service.GoogleAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/appointment/api/google")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    public GoogleAuthController(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    /**
     * Doctor clicks "Connect Google Calendar"
     */
    @GetMapping("/connect")
    public ResponseEntity<Void> connectGoogleCalendar(
            @RequestParam Long doctorId) {

        String authorizationUrl =
                googleAuthService.buildAuthorizationUrl(doctorId);

//        log.info();
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(authorizationUrl))
                .build();
    }

    @GetMapping("/callback")
    public String callback(@RequestParam String code,
                           @RequestParam Long state) throws IOException {
        // state is doctorId
        googleAuthService.handleCallback(code, state);
        return "Google Calendar connected successfully!";
    }
}

