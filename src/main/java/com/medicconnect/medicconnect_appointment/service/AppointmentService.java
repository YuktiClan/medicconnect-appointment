package com.medicconnect.medicconnect_appointment.service;

import com.medicconnect.medicconnect_appointment.dto.AppointmentCreateRequestDTO;
import com.medicconnect.medicconnect_appointment.dto.AppointmentResponseDTO;
import com.medicconnect.medicconnect_appointment.dto.RescheduleRequestDTO;
import com.medicconnect.medicconnect_appointment.mapper.AppointmentMapper;
import com.medicconnect.medicconnect_appointment.model.Appointment;
import com.medicconnect.medicconnect_appointment.model.AppointmentSlotm;
import com.medicconnect.medicconnect_appointment.model.AppointmentStatus;
import com.medicconnect.medicconnect_appointment.repo.OrganizationRepository;
import com.medicconnect.medicconnect_appointment.repo.PatientRepository;
import com.medicconnect.medicconnect_appointment.repo.PersonRepository;
import com.medicconnect.medicconnect_appointment.repository.AppointmentRepository;
import com.medicconnect.medicconnect_appointment.repository.AppointmentSlotRepositorym;
import com.medicconnect.medicconnect_appointment.validator.AppointmentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AppointmentService handles:
 * - creating appointment by reserving slot (atomic using PESSIMISTIC_WRITE on slot)
 * - cancelling appointment and releasing slot
 * - rescheduling (release + reserve)
 *
 * Note: PersonService/PatientService checks should be done at caller level or here if desired.
 */
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotRepositorym slotRepository;
    private final AppointmentValidator validator;
    private final PatientRepository patientRepository;
    private final PersonRepository personRepository;
    private final OrganizationRepository organizationRepository;

    /**
     * Create appointment: reserve slot atomically.
     */
    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentCreateRequestDTO req, String createdBy) {
        validator.validateCreate(req);

        // basic existence checks
        organizationRepository.findById(req.getOrganizationId()).orElseThrow(() -> new IllegalArgumentException("Organization not found"));
        personRepository.findById(req.getDoctorId()).orElseThrow(() -> new IllegalArgumentException("Doctor (Person) not found"));
        patientRepository.findById(req.getPatientId()).orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        // if slotId provided -> attempt to reserve that slot
        if (req.getSlotId() != null) {
            AppointmentSlotm slot = slotRepository.findById(req.getSlotId())
                    .orElseThrow(() -> new IllegalArgumentException("Slot not found"));
            if (!slot.getOrganizationId().equals(req.getOrganizationId())) {
                throw new IllegalArgumentException("Slot does not belong to organization");
            }
            if (slot.getReservedCount() >= slot.getCapacity()) {
                throw new IllegalStateException("Slot is full");
            }
            // reserve
            slot.setReservedCount(slot.getReservedCount() + 1);
            slotRepository.save(slot);

            Appointment appt = Appointment.builder()
                    .organizationId(req.getOrganizationId())
                    .patientId(req.getPatientId())
                    .doctorId(req.getDoctorId())
                    .slotId(slot.getId())
                    .reason(req.getReason())
                    .createdBy(createdBy)
                    .status(AppointmentStatus.SCHEDULED)
                    .build();
            Appointment saved = appointmentRepository.save(appt);
            return AppointmentMapper.toDto(saved);
        }

        // if no slot specified -> find next available slot for doctor in organization
        List<AppointmentSlotm> slots = slotRepository.findByOrganizationIdAndDoctorId(req.getOrganizationId(), req.getDoctorId());
        // naive approach: iterate and try first slot that has capacity (locking each)
        for (AppointmentSlotm s : slots) {
            AppointmentSlotm locked = slotRepository.findById(s.getId()).orElse(null);
            if (locked == null) continue;
            if (locked.getReservedCount() < locked.getCapacity()) {
                locked.setReservedCount(locked.getReservedCount() + 1);
                slotRepository.save(locked);
                Appointment appt = Appointment.builder()
                        .organizationId(req.getOrganizationId())
                        .patientId(req.getPatientId())
                        .doctorId(req.getDoctorId())
                        .slotId(locked.getId())
                        .reason(req.getReason())
                        .createdBy(createdBy)
                        .status(AppointmentStatus.SCHEDULED)
                        .build();
                Appointment saved = appointmentRepository.save(appt);
                return AppointmentMapper.toDto(saved);
            }
        }
        throw new IllegalStateException("No available slot found for doctor");
    }

    /**
     * Cancel appointment: update status and release slot quota
     */
    @Transactional
    public void cancelAppointment(Long appointmentId, String cancelledBy) {
        Appointment appt = appointmentRepository.findById(appointmentId).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        if (appt.getStatus() == AppointmentStatus.CANCELLED) return;
        // lock slot and decrement reservedCount
        AppointmentSlotm slot = slotRepository.findById(appt.getSlotId()).orElseThrow(() -> new IllegalArgumentException("Slot not found"));
        slot.setReservedCount(Math.max(0, slot.getReservedCount() - 1));
        slotRepository.save(slot);
        appt.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appt);
    }

    /**
     * Reschedule: atomically reserve new slot and release old slot (both locked)
     */
    @Transactional
    public AppointmentResponseDTO reschedule(RescheduleRequestDTO req, String requestedBy) {
        validator.validateReschedule(req.getAppointmentId(), req.getNewSlotId());
        Appointment appt = appointmentRepository.findById(req.getAppointmentId()).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        if (appt.getStatus() == AppointmentStatus.CANCELLED) throw new IllegalStateException("Cancelled appointment cannot be rescheduled");

        // lock both new and old slots
        AppointmentSlotm newSlot = slotRepository.findById(req.getNewSlotId()).orElseThrow(() -> new IllegalArgumentException("New slot not found"));
        AppointmentSlotm oldSlot = slotRepository.findById(appt.getSlotId()).orElseThrow(() -> new IllegalArgumentException("Old slot not found"));

        if (!newSlot.getOrganizationId().equals(appt.getOrganizationId())) throw new IllegalArgumentException("Slot org mismatch");

        if (newSlot.getReservedCount() >= newSlot.getCapacity()) {
            throw new IllegalStateException("New slot is full");
        }

        // reserve new slot and release old
        newSlot.setReservedCount(newSlot.getReservedCount() + 1);
        oldSlot.setReservedCount(Math.max(0, oldSlot.getReservedCount() - 1));
        slotRepository.save(newSlot);
        slotRepository.save(oldSlot);

        appt.setSlotId(newSlot.getId());
        appt.setReason(req.getReason());
        appt.setStatus(AppointmentStatus.RESCHEDULED);
        Appointment saved = appointmentRepository.save(appt);
        return AppointmentMapper.toDto(saved);
    }

    public List<AppointmentResponseDTO> listByPatient(Long orgId, Long patientId) {
        List<Appointment> list = appointmentRepository.findByOrganizationIdAndPatientId(orgId, patientId);
        return list.stream().map(AppointmentMapper::toDto).collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> listByDoctorAndRange(Long orgId, Long doctorId, java.time.Instant from, java.time.Instant to) {
        List<Appointment> list = appointmentRepository.findByOrganizationIdAndDoctorIdAndCreatedAtBetween(orgId, doctorId, from, to);
        return list.stream().map(AppointmentMapper::toDto).collect(Collectors.toList());
    }
}
