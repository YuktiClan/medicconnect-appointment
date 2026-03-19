package com.medicconnect.medicconnect_appointment.repo;

import com.medicconnect.medicconnect_appointment.model.DiseaseTrend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiseaseTrendRepository extends JpaRepository<DiseaseTrend, Long> {

    Optional<DiseaseTrend> findByLocationIdAndDiseaseCode(Long locationId, String diseaseCode);

    List<DiseaseTrend> findTop3ByLocationIdOrderByCaseCountDesc(Long locationId);
}
