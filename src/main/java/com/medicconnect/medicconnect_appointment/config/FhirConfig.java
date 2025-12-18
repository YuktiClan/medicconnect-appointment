package com.medicconnect.medicconnect_appointment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.Collections;

@Configuration
public class FhirConfig {

    @Bean
    public MappingJackson2HttpMessageConverter fhirJsonConverter() {
        // Standard Jackson ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

        // Accept FHIR JSON media type
        converter.setSupportedMediaTypes(
                Collections.singletonList(MediaType.valueOf("application/fhir+json"))
        );

        return converter;
    }
}


