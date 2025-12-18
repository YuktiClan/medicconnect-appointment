package com.medicconnect.medicconnect_appointment.repo;

//import org.hl7.fhir.r4.model.Organization;
import com.medicconnect.medicconnect_appointment.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByUuid(String uuid);
    boolean existsByLegalName(String legalName);
}
