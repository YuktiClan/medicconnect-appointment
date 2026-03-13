package com.medicconnect.medicconnect_appointment.controller;

import com.medicconnect.medicconnect_appointment.security.AuthenticatedUser;
import com.medicconnect.medicconnect_appointment.service.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private JwtService jwtService;

    @GetMapping("/debug")
    public String me(Authentication authentication) {

        AuthenticatedUser user =
                (AuthenticatedUser) authentication.getPrincipal();

        return "User ID: " + user.getUserId();
    }
}
