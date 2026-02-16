package com.medicconnect.medicconnect_appointment.controller;

import com.medicconnect.medicconnect_appointment.dto.AddOrganizationMedicineRequest;
import com.medicconnect.medicconnect_appointment.dto.MedicineDropdownDto;
import com.medicconnect.medicconnect_appointment.dto.MedicineResponseDto;
import com.medicconnect.medicconnect_appointment.model.OrganizationMedicine;
import com.medicconnect.medicconnect_appointment.service.OrganizationMedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicines")
@RequiredArgsConstructor
public class OrganizationMedicineController {

    private final OrganizationMedicineService service;

    @PostMapping("/organization")
    public MedicineResponseDto addMedicine(
            @RequestBody AddOrganizationMedicineRequest request) {
        return service.addMedicine(request);
    }

    @GetMapping
    public List<MedicineDropdownDto> getMedicines(
            @RequestParam Long organizationId) {
        return service.getMedicines(organizationId);
    }
}

