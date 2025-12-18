package com.medicconnect.medicconnect_appointment.repo;

//import org.hl7.fhir.r4.model.Patient;
import com.medicconnect.medicconnect_appointment.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUuid(String uuid);
    Optional<Patient> findByPatientIdAndOrganizationId(String patientId, Long organizationId);
    List<Patient> findByOrganizationIdAndAbhaId(Long organizationId, String abhaId);
    List<Patient> findByOrganizationIdAndPersonId(Long organizationId, Long personId);
    List<Patient> findByAbhaId(String abhaId);
}