package com.medicconnect.medicconnect_appointment.service;

import com.medicconnect.medicconnect_appointment.dto.AppointmentResponseDTO;
import com.medicconnect.medicconnect_appointment.dto.CreateAppointmentRequestDTO;
import com.medicconnect.medicconnect_appointment.model.*;
import com.medicconnect.medicconnect_appointment.repo.AppointmentSlotRepository;
import com.medicconnect.medicconnect_appointment.repo.OrganizationRepository;
import com.medicconnect.medicconnect_appointment.repo.PatientRepository;
import com.medicconnect.medicconnect_appointment.repo.PersonRepository;
import com.medicconnect.medicconnect_appointment.repository.AppointmentRepository;
import com.medicconnect.medicconnect_appointment.validator.AppointmentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

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
    private final AppointmentSlotRepository slotRepository;
    private final AppointmentValidator validator;
    private final PatientRepository patientRepository;
    private final PersonRepository personRepository;
    private final OrganizationRepository organizationRepository;

//    /**
//     * Create appointment: reserve slot atomically.
//     */
//    @Transactional
//    public AppointmentResponseDTO createAppointment(AppointmentCreateRequestDTO req, String createdBy) {
//        validator.validateCreate(req);
//
//        // basic existence checks
//        organizationRepository.findById(req.getOrganizationId()).orElseThrow(() -> new IllegalArgumentException("Organization not found"));
//        personRepository.findById(req.getDoctorId()).orElseThrow(() -> new IllegalArgumentException("Doctor (Person) not found"));
//        patientRepository.findById(req.getPatientId()).orElseThrow(() -> new IllegalArgumentException("Patient not found"));
//
//        // if slotId provided -> attempt to reserve that slot
//        if (req.getSlotId() != null) {
//            AppointmentSlotm slot = slotRepository.findById(req.getSlotId())
//                    .orElseThrow(() -> new IllegalArgumentException("Slot not found"));
//            if (!slot.getOrganizationId().equals(req.getOrganizationId())) {
//                throw new IllegalArgumentException("Slot does not belong to organization");
//            }
//            if (slot.getReservedCount() >= slot.getCapacity()) {
//                throw new IllegalStateException("Slot is full");
//            }
//            // reserve
//            slot.setReservedCount(slot.getReservedCount() + 1);
//            slotRepository.save(slot);
//
//            Appointment appt = Appointment.builder()
//                    .organizationId(req.getOrganizationId())
//                    .patientId(req.getPatientId())
//                    .doctorId(req.getDoctorId())
//                    .slotId(slot.getId())
//                    .reason(req.getReason())
//                    .createdBy(createdBy)
//                    .status(AppointmentStatus.SCHEDULED)
//                    .build();
//            Appointment saved = appointmentRepository.save(appt);
//            return AppointmentMapper.toDto(saved);
//        }
//
//        // if no slot specified -> find next available slot for doctor in organization
//        List<AppointmentSlotm> slots = slotRepository.findByOrganizationIdAndDoctorId(req.getOrganizationId(), req.getDoctorId());
//        // naive approach: iterate and try first slot that has capacity (locking each)
//        for (AppointmentSlotm s : slots) {
//            AppointmentSlotm locked = slotRepository.findById(s.getId()).orElse(null);
//            if (locked == null) continue;
//            if (locked.getReservedCount() < locked.getCapacity()) {
//                locked.setReservedCount(locked.getReservedCount() + 1);
//                slotRepository.save(locked);
//                Appointment appt = Appointment.builder()
//                        .organizationId(req.getOrganizationId())
//                        .patientId(req.getPatientId())
//                        .doctorId(req.getDoctorId())
//                        .slotId(locked.getId())
//                        .reason(req.getReason())
//                        .createdBy(createdBy)
//                        .status(AppointmentStatus.SCHEDULED)
//                        .build();
//                Appointment saved = appointmentRepository.save(appt);
//                return AppointmentMapper.toDto(saved);
//            }
//        }
//        throw new IllegalStateException("No available slot found for doctor");
//    }

//    public AppointmentResponseDTO createAppointment(Long doctorId, CreateAppointmentRequestDTO request) {
//
//        // STEP 1: Validate request
//        if (request.getSlotId() == null || request.getPatientId() == null) {
//            throw new RuntimeException("Invalid appointment request");
//        }
//
//        // STEP 2: Fetch slot
//        AppointmentSlot slot = slotRepository.findById(request.getSlotId())
//                .orElseThrow(() -> new RuntimeException("Slot not found"));
//
//        // STEP 3: Validate slot belongs to doctor
//        if (!slot.getDoctorSchedule().getDoctorId().equals(doctorId)) {
//            throw new RuntimeException("Slot does not belong to this doctor");
//        }
//
////        // STEP 4: Check if slot is active
////        if (!slot.isActive()) {
////            throw new RuntimeException("Slot is inactive");
////        }
//
//        // STEP 5: Check if slot already booked
//        boolean alreadyBooked =
//                appointmentRepository.existsBySlotIdAndStatus(
//                        slot.getId(),
//                        AppointmentStatus.BOOKED
//                );
//
//        if (alreadyBooked) {
//            throw new RuntimeException("Slot already booked");
//        }
//
//        // STEP 6: Create appointment entity
//        Appointment appointment = new Appointment();
//        appointment.setUuid(UUID.randomUUID().toString());
////        appointment.setOrganizationId(request.getOrganizationId());
//        appointment.setDoctorId(doctorId);
//        appointment.setPatientId(request.getPatientId());
//        appointment.setSlotId(slot.getId());
//        appointment.setStatus(AppointmentStatus.BOOKED);
//        appointment.setCreatedAt(Instant.now());
//        appointment.setCreatedBy("SYSTEM"); // replace with auth user if available
//
//        // STEP 7: Save appointment
//        Appointment savedAppointment = appointmentRepository.save(appointment);
//
//        // STEP 8: Mark slot as BOOKED
//        slot.setStatus(SlotStatus.BOOKED);
//        slotRepository.save(slot);
//
//        // STEP 9: Prepare response DTO
//        AppointmentResponseDTO response = new AppointmentResponseDTO();
//        response.setAppointmentId(savedAppointment.getId());
//        response.setUuid(savedAppointment.getUuid());
//        response.setOrganizationId(savedAppointment.getOrganizationId());
//        response.setDoctorId(savedAppointment.getDoctorId());
//        response.setPatientId(savedAppointment.getPatientId());
//        response.setSlotId(savedAppointment.getSlotId());
//        response.setStatus(savedAppointment.getStatus().name());
//        response.setReason(savedAppointment.getReason());
//        response.setCreatedBy(savedAppointment.getCreatedBy());
//        response.setCreatedAt(savedAppointment.getCreatedAt());
//        response.setCheckedInAt(savedAppointment.getCheckedInAt());
//        response.setStartTime(slot.getStartTime().toString());
//        response.setEndTime(slot.getEndTime().toString());
//
//        return response;
//    }
//
//    /**
//     * Cancel appointment: update status and release slot quota
//     */
//    @Transactional
//    public void cancelAppointment(Long appointmentId, String cancelledBy) {
//        Appointment appt = appointmentRepository.findById(appointmentId).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
//        if (appt.getStatus() == AppointmentStatus.CANCELLED) return;
//        // lock slot and decrement reservedCount
//        AppointmentSlotm slot = slotRepository.findById(appt.getSlotId()).orElseThrow(() -> new IllegalArgumentException("Slot not found"));
//        slot.setReservedCount(Math.max(0, slot.getReservedCount() - 1));
//        slotRepository.save(slot);
//        appt.setStatus(AppointmentStatus.CANCELLED);
//        appointmentRepository.save(appt);
//    }

//    /**
//     * Reschedule: atomically reserve new slot and release old slot (both locked)
//     */
//    @Transactional
//    public AppointmentResponseDTO reschedule(RescheduleRequestDTO req, String requestedBy) {
//        validator.validateReschedule(req.getAppointmentId(), req.getNewSlotId());
//        Appointment appt = appointmentRepository.findById(req.getAppointmentId()).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
//        if (appt.getStatus() == AppointmentStatus.CANCELLED) throw new IllegalStateException("Cancelled appointment cannot be rescheduled");
//
//        // lock both new and old slots
//        AppointmentSlotm newSlot = slotRepository.findById(req.getNewSlotId()).orElseThrow(() -> new IllegalArgumentException("New slot not found"));
//        AppointmentSlotm oldSlot = slotRepository.findById(appt.getSlotId()).orElseThrow(() -> new IllegalArgumentException("Old slot not found"));
//
//        if (!newSlot.getOrganizationId().equals(appt.getOrganizationId())) throw new IllegalArgumentException("Slot org mismatch");
//
//        if (newSlot.getReservedCount() >= newSlot.getCapacity()) {
//            throw new IllegalStateException("New slot is full");
//        }
//
//        // reserve new slot and release old
//        newSlot.setReservedCount(newSlot.getReservedCount() + 1);
//        oldSlot.setReservedCount(Math.max(0, oldSlot.getReservedCount() - 1));
//        slotRepository.save(newSlot);
//        slotRepository.save(oldSlot);
//
//        appt.setSlotId(newSlot.getId());
//        appt.setReason(req.getReason());
//        appt.setStatus(AppointmentStatus.RESCHEDULED);
//        Appointment saved = appointmentRepository.save(appt);
//        return AppointmentMapper.toDto(saved);
//    }
//
//    public List<AppointmentResponseDTO> listByPatient(Long orgId, Long patientId) {
//        List<Appointment> list = appointmentRepository.findByOrganizationIdAndPatientId(orgId, patientId);
//        return list.stream().map(AppointmentMapper::toDto).collect(Collectors.toList());
//    }
//
//    public List<AppointmentResponseDTO> listByDoctorAndRange(Long orgId, Long doctorId, java.time.Instant from, java.time.Instant to) {
//        List<Appointment> list = appointmentRepository.findByOrganizationIdAndDoctorIdAndCreatedAtBetween(orgId, doctorId, from, to);
//        return list.stream().map(AppointmentMapper::toDto).collect(Collectors.toList());
//    }
}
