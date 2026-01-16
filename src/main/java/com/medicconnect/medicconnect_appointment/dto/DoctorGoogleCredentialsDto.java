package com.medicconnect.medicconnect_appointment.dto;

import lombok.Data;

@Data
public class DoctorGoogleCredentialsDto {
    String doctorId;
    String accessToken;
    String refreshToken;
}

