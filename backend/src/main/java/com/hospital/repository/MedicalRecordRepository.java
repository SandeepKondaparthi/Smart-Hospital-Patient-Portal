package com.hospital.repository;

import com.hospital.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    @Query("SELECT m FROM MedicalRecord m JOIN FETCH m.patient p JOIN FETCH p.user " +
           "JOIN FETCH m.doctor d JOIN FETCH d.user WHERE m.patient.id = :patientId " +
           "ORDER BY m.visitDate DESC")
    List<MedicalRecord> findByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT m FROM MedicalRecord m JOIN FETCH m.patient p JOIN FETCH p.user " +
           "JOIN FETCH m.doctor d JOIN FETCH d.user WHERE m.id = :id")
    Optional<MedicalRecord> findByIdWithDetails(@Param("id") Long id);

    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);
}
