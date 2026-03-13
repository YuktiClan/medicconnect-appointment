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
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;



    // week or month
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

            List<MealTime> mealTimes = dto.getTime()
                    .stream()
                    .map(MealTime::fromCode)
                    .toList();
            MedicineItem item = MedicineItem.builder()
                    .name(dto.getName())
                    .dosage(dto.getDosage())
//                    .frequency(dto.getFrequency())
                    .duration(dto.getDuration())
                    .mealTime(mealTimes)
                    .frequencyType(FrequencyType.fromCode(dto.getFrequency()))
                    .prescription(prescription)
                    .instructions(Objects.nonNull(dto.getInstructions()) ? dto.getInstructions() : "")
                    .build();
            prescription.getMedicines().add(item);
        }

        Prescription saved = prescriptionRepository.save(prescription);

        // Convert back to response DTO
        List<CreatePrescriptionRequest.MedicineItemDto> responseMedicines =
                saved.getMedicines().stream()
                        .map(m -> new CreatePrescriptionRequest.MedicineItemDto(
                                m.getName(), m.getDosage(), m.getFrequencyType().getCode(), m.getDuration(),m.getMealTime().stream().map(MealTime::getCode).toList(), m.getInstructions()

                        ))
                        .toList();

        return  PrescriptionResponse.builder()
                .prescriptionId(saved.getId())
                .appointmentId(appointment.getId())
                .medicines(responseMedicines)
                .notes(saved.getNotes())
                .build();
    }

    public PrescriptionResponse getPrescription(Long prescriptionId) {

        Prescription prescription = prescriptionRepository
                .findByIdWithMedicines(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));

        // Convert Medicines to DTO
        List<CreatePrescriptionRequest.MedicineItemDto> medicineDtos =
                prescription.getMedicines().stream()
                        .map(m -> new CreatePrescriptionRequest.MedicineItemDto(
                                m.getName(),
                                m.getDosage(),
                                Objects.nonNull(m.getFrequencyType()) ? m.getFrequencyType().getCode() : null,
                                m.getDuration(),
                                Objects.nonNull(m.getMealTime()) ? m.getMealTime().stream().map(MealTime::getCode).toList() : null,
                                Objects.nonNull(m.getInstructions()) ? m.getInstructions() : ""
                        ))
                        .toList();

        return PrescriptionResponse.builder()
                .prescriptionId(prescription.getId())
                .appointmentId(prescription.getAppointment().getId())
                .notes(prescription.getNotes())
                .medicines(medicineDtos)
                .build();
    }

}

