package com.medicconnect.medicconnect_appointment.controller;

import com.medicconnect.medicconnect_appointment.dto.CreatePrescriptionRequest;
import com.medicconnect.medicconnect_appointment.dto.MedicineDropdownDto;
import com.medicconnect.medicconnect_appointment.dto.PrescriptionResponse;
import com.medicconnect.medicconnect_appointment.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointment")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping("/{appointmentId}/prescription")
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @PathVariable Long appointmentId,
            @RequestBody CreatePrescriptionRequest request
    ) {
        // Ensure path variable matches request
        request.setAppointmentId(appointmentId);
        PrescriptionResponse response = prescriptionService.createPrescription(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> getPrescription(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                prescriptionService.getPrescription(id)
        );
    }

}

