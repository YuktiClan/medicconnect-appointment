package com.medicconnect.medicconnect_appointment.service;

import com.google.api.services.calendar.model.AclRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalendarSharingService {

    private final GoogleCalendarService googleCalendarService;

    public void shareCalendarWithAdmin(String refreshToken, String adminEmail)
            throws Exception {

        com.google.api.services.calendar.Calendar calendarService =
                googleCalendarService.getCalendarService(refreshToken);

        AclRule.Scope scope = new AclRule.Scope();
        scope.setType("user");
        scope.setValue(adminEmail);

        AclRule rule = new AclRule();
        rule.setScope(scope);
        rule.setRole("writer");

        calendarService.acl()
                .insert("primary", rule)
                .execute();
    }
}

