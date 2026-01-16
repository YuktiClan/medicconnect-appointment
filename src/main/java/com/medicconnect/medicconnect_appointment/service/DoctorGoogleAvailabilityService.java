package com.medicconnect.medicconnect_appointment.service;


import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import com.medicconnect.medicconnect_appointment.model.DoctorGoogleToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;

@Slf4j
@Service
public class DoctorGoogleAvailabilityService {

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;


    private final DoctorGoogleCredentialService doctorService;

    public DoctorGoogleAvailabilityService(DoctorGoogleCredentialService doctorService) {
        this.doctorService = doctorService;
    }

    private Calendar buildCalendar(DoctorGoogleToken creds) throws Exception {

        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(googleClientId)
                .setClientSecret(googleClientSecret)
                .setAccessToken(new AccessToken(creds.getAccessToken(), null))
                .setRefreshToken(creds.getRefreshToken())
                .build();

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName("MedicConnect").build();
    }
    public void validateDoctorAvailabilityOnGoogle(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime slotStartTime,
            int slotDurationMinutes
    ) throws Exception {

        DoctorGoogleToken creds = doctorService.getGoogleCredentials(doctorId);
        if (creds == null) return;

        Calendar calendar = buildCalendar(creds);

        Instant start = appointmentDate
                .atTime(slotStartTime)
                .atZone(ZoneId.of("Asia/Kolkata"))
                .toInstant();

        Instant end = start.plusSeconds(slotDurationMinutes * 60L);

        Events events = calendar.events().list("primary")
                .setTimeMin(new DateTime(start.toEpochMilli()))
                .setTimeMax(new DateTime(end.toEpochMilli()))
                .setSingleEvents(true)
                .execute();

        if (events.getItems() != null && !events.getItems().isEmpty()) {
            throw new RuntimeException("Doctor is busy on Google Calendar");
        }
    }

    public void createDoctorEventOnGoogle(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime slotStartTime,
            int slotDurationMinutes
    ) throws Exception {

        DoctorGoogleToken creds = doctorService.getGoogleCredentials(doctorId);
        if (creds == null) return;

        Calendar calendar = buildCalendar(creds);

        Instant start = appointmentDate
                .atTime(slotStartTime)
                .atZone(ZoneId.of("Asia/Kolkata"))
                .toInstant();

        Instant end = start.plusSeconds(slotDurationMinutes * 60L);

        Event event = new Event()
                .setSummary("Doctor Appointment")
                .setStart(new EventDateTime()
                        .setDateTime(new DateTime(start.toEpochMilli()))
                        .setTimeZone("Asia/Kolkata"))
                .setEnd(new EventDateTime()
                        .setDateTime(new DateTime(end.toEpochMilli()))
                        .setTimeZone("Asia/Kolkata"));

        calendar.events().insert("primary", event).execute();
    }


}

