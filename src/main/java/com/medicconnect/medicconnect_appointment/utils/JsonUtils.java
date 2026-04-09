package com.medicconnect.medicconnect_appointment.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicconnect.medicconnect_appointment.dto.SymptomDTO;
import com.medicconnect.medicconnect_appointment.dto.TestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JsonUtils {

    private final ObjectMapper objectMapper;

    public List<SymptomDTO> parseSymptoms(String symptomsJson) {
        try {
            if (symptomsJson == null || symptomsJson.isEmpty()) {
                return Collections.emptyList();
            }

            return objectMapper.readValue(
                    symptomsJson,
                    new TypeReference<List<SymptomDTO>>() {}
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse symptoms JSON", e);
        }
    }

    public List<TestDto> parseTests(String testsJson) {
        try {
            if (testsJson == null || testsJson.isEmpty()) {
                return Collections.emptyList();
            }

            if (testsJson.startsWith("\"")) {
                testsJson = objectMapper.readValue(testsJson, String.class);
            }

            return objectMapper.readValue(
                    testsJson,
                    new TypeReference<List<TestDto>>() {}
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Tests JSON", e);
        }
    }
}
