package com.medicconnect.medicconnect_appointment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;

@Configuration
public class FhirWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter fhirConverter =
                new MappingJackson2HttpMessageConverter(new ObjectMapper());

        fhirConverter.setSupportedMediaTypes(
                Collections.singletonList(MediaType.valueOf("application/fhir+json"))
        );

        // Add at the beginning
        converters.add(0, fhirConverter);
    }
}


