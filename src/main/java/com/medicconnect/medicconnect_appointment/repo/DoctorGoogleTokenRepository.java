package com.medicconnect.medicconnect_appointment.repo;

import com.medicconnect.medicconnect_appointment.model.DoctorGoogleToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorGoogleTokenRepository
        extends JpaRepository<DoctorGoogleToken, Long> {

    Optional<DoctorGoogleToken> findByDoctorId(Long doctorId);
}

