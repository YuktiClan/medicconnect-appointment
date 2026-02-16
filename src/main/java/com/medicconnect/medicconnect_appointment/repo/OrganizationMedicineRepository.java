package com.medicconnect.medicconnect_appointment.repo;

import com.medicconnect.medicconnect_appointment.model.OrganizationMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationMedicineRepository extends JpaRepository<OrganizationMedicine, Long> {

    List<OrganizationMedicine> findByOrganizationId(Long organizationId);

}
