package com.medicconnect.medicconnect_appointment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCreateRequestDTO {
    private Long organizationId;
    private Long patientId;
    private Long doctorId;
    private Long slotId;          // preferred slot (if null, service may search)
    private String reason;
}
