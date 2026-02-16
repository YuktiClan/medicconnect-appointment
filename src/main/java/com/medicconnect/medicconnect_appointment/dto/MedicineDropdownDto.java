package com.medicconnect.medicconnect_appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MedicineDropdownDto {
    private Long id;
    private String name;
    private String source; // MASTER | ORGANIZATION
}
