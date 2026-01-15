package com.medicconnect.medicconnect_appointment.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_google_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorGoogleToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "access_token", columnDefinition = "TEXT", nullable = false)
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT", nullable = false)
    private String refreshToken;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;
}

