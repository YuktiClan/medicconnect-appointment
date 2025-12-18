package com.medicconnect.medicconnect_appointment.dto;

import com.medicconnect.medicconnect_appointment.model.AppointmentStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponseDTO {
    private Long id;
    private String uuid;
    private Long organizationId;
    private Long patientId;
    private Long doctorId;
    private Long slotId;
    private AppointmentStatus status;
    private String reason;
    private String createdBy;
    private Instant createdAt;
    private Instant checkedInAt;
}
