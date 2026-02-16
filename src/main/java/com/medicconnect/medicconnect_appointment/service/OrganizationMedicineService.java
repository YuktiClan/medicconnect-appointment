package com.medicconnect.medicconnect_appointment.service;

import com.medicconnect.medicconnect_appointment.dto.AddOrganizationMedicineRequest;
import com.medicconnect.medicconnect_appointment.dto.MedicineDropdownDto;
import com.medicconnect.medicconnect_appointment.dto.MedicineResponseDto;
import com.medicconnect.medicconnect_appointment.model.OrganizationMedicine;
import com.medicconnect.medicconnect_appointment.repo.MasterMedicineRepository;
import com.medicconnect.medicconnect_appointment.repo.OrganizationMedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationMedicineService {

    private final OrganizationMedicineRepository repository;
    private final MasterMedicineRepository masterRepo;
    private final OrganizationMedicineRepository orgRepo;


    public MedicineResponseDto addMedicine(AddOrganizationMedicineRequest request) {

        OrganizationMedicine medicine = OrganizationMedicine.builder()
                .name(request.getName())
                .description(request.getDescription())
                .doctorId(request.getDoctorId())
                .organizationId(request.getOrganizationId())
                .createdAt(LocalDateTime.now())
                .build();
        List<OrganizationMedicine> byOrganizationId = repository.findByOrganizationId(request.getOrganizationId());
        byOrganizationId.forEach(n -> {
            if (n.getName().equalsIgnoreCase(request.getName())) {
                throw new RuntimeException("Already exist with your organisation.");
            }
        });

        OrganizationMedicine savedMedicine = repository.save(medicine);

        return MedicineResponseDto.builder()
                .id(savedMedicine.getId())
                .name(savedMedicine.getName())
                .description(savedMedicine.getDescription())
                .organizationId(savedMedicine.getOrganizationId().toString())
                .build();
    }

    public List<MedicineDropdownDto> getMedicines(Long organizationId) {

        List<MedicineDropdownDto> result = new ArrayList<>();

        masterRepo.findAll().forEach(m ->
                result.add(new MedicineDropdownDto(m.getId(), m.getName(), "MASTER"))
        );

        orgRepo.findByOrganizationId(organizationId).forEach(m ->
                result.add(new MedicineDropdownDto(m.getId(), m.getName(), "ORGANIZATION"))
        );

        return result;
    }
}

