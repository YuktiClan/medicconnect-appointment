package com.medicconnect.medicconnect_appointment.validator;

import com.medicconnect.medicconnect_appointment.dto.AppointmentCreateRequestDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class AppointmentValidator {

    public void validateCreate(AppointmentCreateRequestDTO dto) {
        Assert.notNull(dto, "request is required");
        Assert.notNull(dto.getOrganizationId(), "organizationId is required");
        Assert.notNull(dto.getPatientId(), "patientId is required");
        Assert.notNull(dto.getDoctorId(), "doctorId is required");
        // slotId may be null (service can pick)
    }

    public void validateReschedule(Long appointmentId, Long newSlotId) {
        Assert.notNull(appointmentId, "appointmentId is required");
        Assert.notNull(newSlotId, "newSlotId is required");
    }
}
