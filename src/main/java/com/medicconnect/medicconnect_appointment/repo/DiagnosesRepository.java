package com.medicconnect.medicconnect_appointment.repo;

import com.medicconnect.medicconnect_appointment.model.AppointmentDiagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosesRepository extends JpaRepository<AppointmentDiagnosis, Long> {

    void deleteByAppointmentId(Long appointmentId);

    List<AppointmentDiagnosis> findByAppointmentId(Long appointmentId);

}
