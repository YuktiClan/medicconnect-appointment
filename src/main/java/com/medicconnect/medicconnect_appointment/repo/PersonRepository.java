package com.medicconnect.medicconnect_appointment.repo;

//import org.hl7.fhir.r4.model.Person;
import com.medicconnect.medicconnect_appointment.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByUuid(String uuid);
    List<Person> findByGivenNameContainingIgnoreCaseOrFamilyNameContainingIgnoreCase(String given, String family);
    Optional<Person> findByGovernmentId(String governmentId);
}
