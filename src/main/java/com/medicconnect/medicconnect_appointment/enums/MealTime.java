package com.medicconnect.medicconnect_appointment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum MealTime {

    BEFORE_BREAKFAST(1),
    AFTER_BREAKFAST(2),
    AFTER_LUNCH(3),
    AFTER_DINNER(4),
    BEFORE_BED(5);

    private final int code;

    public static MealTime fromCode(Integer code) {
        if (code == null) return null;

        return Arrays.stream(values())
                .filter(v -> v.code == code)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid MealTime code: " + code));
    }
}

