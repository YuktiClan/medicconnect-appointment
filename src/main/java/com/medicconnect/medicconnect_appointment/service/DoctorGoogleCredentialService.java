package com.medicconnect.medicconnect_appointment.service;


import com.medicconnect.medicconnect_appointment.model.DoctorGoogleToken;
import com.medicconnect.medicconnect_appointment.repo.DoctorGoogleTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class DoctorGoogleCredentialService {

    private final DoctorGoogleTokenRepository tokenRepository;

    public DoctorGoogleCredentialService(
             DoctorGoogleTokenRepository tokenRepository
    ) {
        this.tokenRepository = tokenRepository;
    }

    public DoctorGoogleToken getGoogleCredentials(Long doctorId) {
        return tokenRepository.findByDoctorId(doctorId).orElse(null);
    }
}

