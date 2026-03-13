package com.medicconnect.medicconnect_appointment.repo;

import com.medicconnect.medicconnect_appointment.model.DiseaseMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DiseaseMasterRepository extends JpaRepository<DiseaseMaster, String> {
    List<DiseaseMaster> findByIsActiveTrue();

    @Query("SELECT d FROM DiseaseMaster d WHERE LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DiseaseMaster> searchDiseases(String keyword);

}
