package com.medicconnect.medicconnect_appointment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class TestDto {

    private String testName;
    private String status;
}