package com.medicconnect.medicconnect_appointment.mapper;


import com.medicconnect.medicconnect_appointment.dto.AppointmentResponseDTO;
import com.medicconnect.medicconnect_appointment.model.Appointment;
import com.medicconnect.medicconnect_appointment.model.AppointmentStatus;

public class AppointmentMapper {
    public static AppointmentResponseDTO toDto(Appointment a) {
        if (a == null) return null;
        return AppointmentResponseDTO.builder()
                .id(a.getId())
//                .uuid(a.getUuid())
//                .organizationId(a.getOrganizationId())
                .patientId(a.getPatientId())
                .doctorId(a.getDoctorId())
//                .slotId(a.getSlotId())
                .status(a.getStatus().name())
//                .reason(a.getReason())
//                .createdBy(a.getCreatedBy())
//                .createdAt(a.getCreatedAt())
//                .checkedInAt(a.getCheckedInAt())
                .build();
    }
}
