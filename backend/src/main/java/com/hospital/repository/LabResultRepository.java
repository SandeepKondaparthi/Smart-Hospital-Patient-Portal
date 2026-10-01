package com.hospital.repository;

import com.hospital.entity.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabResultRepository extends JpaRepository<LabResult, Long> {

    @Query("SELECT l FROM LabResult l LEFT JOIN FETCH l.orderedBy o LEFT JOIN FETCH o.user " +
           "WHERE l.patient.id = :patientId ORDER BY l.testDate DESC")
    List<LabResult> findByPatientId(@Param("patientId") Long patientId);

    List<LabResult> findByMedicalRecordId(Long medicalRecordId);
}
