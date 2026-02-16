package com.medicconnect.medicconnect_appointment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum FrequencyType {

    SOS(6),              // On need basis
    EVERY_N_HOURS(7),
    N_TIMES_PER_DAY(8);

    private final int code;

    public static FrequencyType fromCode(Integer code) {
        if (code == null) return null;


        return Arrays.stream(values())
                .filter(v -> v.code == code)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid Frequency code: " + code));
    }
}

