package com.medicconnect.medicconnect_appointment.service;

import com.medicconnect.medicconnect_appointment.dto.CreatePrescriptionRequest;
import com.medicconnect.medicconnect_appointment.dto.MedicineDropdownDto;
import com.medicconnect.medicconnect_appointment.dto.PrescriptionResponse;
import com.medicconnect.medicconnect_appointment.enums.FrequencyType;
import com.medicconnect.medicconnect_appointment.enums.MealTime;
import com.medicconnect.medicconnect_appointment.model.Appointment;
import com.medicconnect.medicconnect_appointment.model.MedicineItem;
import com.medicconnect.medicconnect_appointment.model.Prescription;
import com.medicconnect.medicconnect_appointment.repo.MasterMedicineRepository;
import com.medicconnect.medicconnect_appointment.repo.OrganizationMedicineRepository;
import com.medicconnect.medicconnect_appointment.repo.PrescriptionRepository;
import com.medicconnect.medicconnect_appointment.repository.AppointmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;



    @Transactional
    public PrescriptionResponse createPrescription(CreatePrescriptionRequest request) {

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctorId().equals(request.getDoctorId())) {
            throw new RuntimeException("Doctor not authorized");
        }

        Prescription prescription = Prescription.builder()
                .appointment(appointment)
                .notes(request.getNotes())
                .medicines(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();


        // Convert DTO medicines to entities
        for (CreatePrescriptionRequest.MedicineItemDto dto : request.getMedicines()) {

            MedicineItem item = MedicineItem.builder()
                    .name(dto.getName())
                    .dosage(dto.getDosage())
//                    .frequency(dto.getFrequency())
                    .duration(dto.getDuration())
                    .mealTime(MealTime.fromCode(dto.getTime()))
                    .frequencyType(FrequencyType.fromCode(dto.getFrequency()))
                    .prescription(prescription)
                    .build();
            prescription.getMedicines().add(item);
        }

        Prescription saved = prescriptionRepository.save(prescription);

        // Convert back to response DTO
        List<CreatePrescriptionRequest.MedicineItemDto> responseMedicines =
                saved.getMedicines().stream()
                        .map(m -> new CreatePrescriptionRequest.MedicineItemDto(
                                m.getName(), m.getDosage(), m.getFrequencyType().getCode(), m.getDuration(),m.getMealTime().getCode()

                        ))
                        .toList();

        return  PrescriptionResponse.builder()
                .prescriptionId(saved.getId())
                .appointmentId(appointment.getId())
                .medicines(responseMedicines)
                .notes(saved.getNotes())
                .build();
    }
}

