package com.medicconnect.medicconnect_appointment.service;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;

import static com.google.api.client.json.gson.GsonFactory.*;

@Service
public class GoogleCalendarService {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    public com.google.api.services.calendar.Calendar getCalendarService(String refreshToken)
            throws Exception {

        GoogleTokenResponse tokenResponse =
                new GoogleRefreshTokenRequest(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance(),
                        refreshToken,
                        clientId,
                        clientSecret
                ).execute();

        Credential credential =
                new Credential(BearerToken.authorizationHeaderAccessMethod())
                        .setAccessToken(tokenResponse.getAccessToken());

        return new com.google.api.services.calendar.Calendar.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        )
                .setApplicationName("Doctor App")
                .build();
    }
}

