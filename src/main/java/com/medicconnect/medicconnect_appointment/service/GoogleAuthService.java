package com.medicconnect.medicconnect_appointment.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.medicconnect.medicconnect_appointment.model.DoctorGoogleToken;
import com.medicconnect.medicconnect_appointment.repo.DoctorGoogleTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private static final String SCOPE =
            "https://www.googleapis.com/auth/calendar";

    private final DoctorGoogleTokenRepository tokenRepository;

    public GoogleAuthService(DoctorGoogleTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String buildAuthorizationUrl(Long doctorId) {

        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=" + encode(SCOPE)
                + "&access_type=offline"
                + "&state=" + doctorId
                + "&prompt=consent";
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public void handleCallback(String code, Long doctorId) throws IOException {

        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                clientId,
                clientSecret,
                code,
                redirectUri
        ).execute();

        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();
        long expiresIn = tokenResponse.getExpiresInSeconds();

        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(expiresIn);

        Optional<DoctorGoogleToken> existingToken = tokenRepository.findByDoctorId(doctorId);

        DoctorGoogleToken token = existingToken.orElse(new DoctorGoogleToken());
        token.setDoctorId(doctorId);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setExpiryTime(expiryTime);

        tokenRepository.save(token);
    }
}

