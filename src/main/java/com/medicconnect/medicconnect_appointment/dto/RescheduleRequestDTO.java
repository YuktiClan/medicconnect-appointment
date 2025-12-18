package com.medicconnect.medicconnect_appointment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescheduleRequestDTO {
    private Long appointmentId;
    private Long newSlotId;
    private String reason;
}
