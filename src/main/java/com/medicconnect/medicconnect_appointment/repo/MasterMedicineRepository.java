package com.medicconnect.medicconnect_appointment.repo;

import com.medicconnect.medicconnect_appointment.model.MasterMedicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterMedicineRepository extends JpaRepository<MasterMedicine,Long> {

    List<MasterMedicine> findByNameContainingIgnoreCase(String keyword);

}
