package com.medicconnect.medicconnect_appointment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddOrganizationMedicineRequest {
    private Long doctorId;
    private Long organizationId;
    private String name;
    private String description;
}

