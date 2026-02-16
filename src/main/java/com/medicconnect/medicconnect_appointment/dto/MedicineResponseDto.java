package com.medicconnect.medicconnect_appointment.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicineResponseDto {
    private Long id;
    private String name;
    private String doctorId;
    private String organizationId;
    private String description;
}
