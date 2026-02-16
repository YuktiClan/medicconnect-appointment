package com.medicconnect.medicconnect_appointment.repo;

import com.medicconnect.medicconnect_appointment.model.MedicineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineItemRepository extends JpaRepository<MedicineItem, Long> {

    // Optional: find all medicines for a prescription
    List<MedicineItem> findByPrescriptionId(Long prescriptionId);

}

