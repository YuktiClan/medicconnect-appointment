package com.medicconnect.medicconnect_appointment.service;

import com.medicconnect.medicconnect_appointment.dto.PageResponse;
import com.medicconnect.medicconnect_appointment.dto.PatientTimelineResponse;
import com.medicconnect.medicconnect_appointment.model.Appointment;
import com.medicconnect.medicconnect_appointment.model.AppointmentStatus;
import com.medicconnect.medicconnect_appointment.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import java.util.Objects;

@Slf4j
@Service
public class PatientAppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public PageResponse<PatientTimelineResponse> getPatientTimeline(
            Long patientId,
            String status,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "appointmentDate")
        );

        AppointmentStatus appointmentStatus =
                Objects.nonNull(status) && !status.isEmpty()
                        ? AppointmentStatus.valueOf(status)
                        : null;
        Page<Appointment> appointmentPage;
        if (Objects.isNull(appointmentStatus)){
            appointmentPage =
                    appointmentRepository.findByPatientId(
                            patientId,
                            pageable
                    );
        }else {
            appointmentPage =
                    appointmentRepository.findByPatientIdAndStatus(
                            patientId,
                            appointmentStatus,
                            pageable
                    );
        }

        List<PatientTimelineResponse> content =
                appointmentPage.getContent()
                        .stream()
                        .map(a -> new PatientTimelineResponse(
                                a.getAppointmentDate().toString(),
                                a.getDoctorId(),
                                a.getDiagnosis(),
                                a.getSymptoms(),
                                a.getPrescription(),
                                a.getTests(),
                                a.getDoctorNotes()
                        ))
                        .toList();

        PageResponse<PatientTimelineResponse> patientTimelineResponsePageResponse = new PageResponse<>(
                content,
                appointmentPage.getNumber(),
                appointmentPage.getSize(),
                appointmentPage.getTotalElements(),
                appointmentPage.getTotalPages(),
                appointmentPage.isLast()
        );
        log.info("response for patient timeline - {} ", patientTimelineResponsePageResponse);
        return patientTimelineResponsePageResponse;
    }
}
