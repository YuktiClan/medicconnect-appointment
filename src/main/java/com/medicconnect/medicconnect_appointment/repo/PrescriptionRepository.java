package com.medicconnect.medicconnect_appointment.repo;

import com.medicconnect.medicconnect_appointment.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    // Optional: find all prescriptions for a specific appointment
    List<Prescription> findByAppointmentId(Long appointmentId);

    @Query("""
           SELECT p FROM Prescription p
           LEFT JOIN FETCH p.medicines
           WHERE p.id = :id
           """)
    Optional<Prescription> findByIdWithMedicines(@Param("id") Long id);

}

