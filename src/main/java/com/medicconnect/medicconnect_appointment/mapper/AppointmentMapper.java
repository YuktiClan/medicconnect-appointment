package com.medicconnect.medicconnect_appointment.mapper;


import com.medicconnect.medicconnect_appointment.dto.AppointmentResponseDTO;
import com.medicconnect.medicconnect_appointment.model.Appointment;
import com.medicconnect.medicconnect_appointment.model.AppointmentStatus;

import java.time.LocalDate;
import java.util.Objects;

public class AppointmentMapper {
    public static AppointmentResponseDTO toDto(Appointment a) {
        if (a == null) return null;
        return AppointmentResponseDTO.builder()
                .appointmentId(a.getId() != null ? a.getId() : 0L)
                .patientId(a.getPatientId() != null ? a.getPatientId() : 0L)
                .doctorId(a.getDoctorId() != null ? a.getDoctorId() : 0L)
                .slotId(a.getSlotNo() != null ? a.getSlotNo() : 0L)
                .status(a.getStatus() != null ? a.getStatus().name() : "UNKNOWN")
                .appointmentDate(a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : "")
                .slotNo(a.getSlotNo() != null ? a.getSlotNo() : 0L)
                .consultationStartedAt(a.getConsultationStartedAt()!= null ? a.getConsultationStartedAt().toString() :"")
                .consultationCompletedAt(a.getConsultationCompletedAt()!= null ? a.getConsultationCompletedAt().toString():"")
                .bloodPressure(a.getBloodPressure() != null ? a.getBloodPressure() : "")
                .pulse(a.getPulse() != null ? a.getPulse() : 0)
                .temperature(a.getTemperature() != null ? a.getTemperature() : 0.0)
                .unit(Objects.nonNull(a.getUnit()) ?a.getUnit() : "")
                .initialComplaints(a.getInitialComplaints() != null ? a.getInitialComplaints() : "")
                .symptoms(a.getSymptoms() != null ? a.getSymptoms() : "")
                .diagnosis(a.getDiagnosis() != null ? a.getDiagnosis() : "")
                .prescription(a.getPrescription() != null ? a.getPrescription() : "")
                .tests(a.getTests() != null ? a.getTests() : "")
                .patientComments(a.getPatientComments() != null ? a.getPatientComments() : "")
                .doctorNotes(a.getDoctorNotes() != null ? a.getDoctorNotes() : "")
                .bloodGroup(a.getBloodGroup() != null ? a.getBloodGroup() : "")
                .weight(a.getWeight() != null ? a.getWeight() : 0)
                .respiratoryRate(a.getRespiratoryRate() != null ? a.getRespiratoryRate() : String.valueOf(0))
                .spo2(a.getSpo2() != null ? a.getSpo2() : "")
                .build();
    }
}
