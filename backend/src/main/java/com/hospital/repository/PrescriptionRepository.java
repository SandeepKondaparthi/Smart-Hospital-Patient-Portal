package com.hospital.repository;

import com.hospital.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @Query("SELECT p FROM Prescription p JOIN FETCH p.patient pt JOIN FETCH pt.user " +
           "JOIN FETCH p.doctor d JOIN FETCH d.user LEFT JOIN FETCH p.items " +
           "WHERE p.patient.id = :patientId ORDER BY p.prescriptionDate DESC")
    List<Prescription> findByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT p FROM Prescription p JOIN FETCH p.patient pt JOIN FETCH pt.user " +
           "JOIN FETCH p.doctor d JOIN FETCH d.user LEFT JOIN FETCH p.items " +
           "WHERE p.id = :id")
    Optional<Prescription> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT p FROM Prescription p JOIN FETCH p.patient pt JOIN FETCH pt.user " +
           "JOIN FETCH p.doctor d JOIN FETCH d.user LEFT JOIN FETCH p.items " +
           "WHERE p.doctor.id = :doctorId ORDER BY p.prescriptionDate DESC")
    List<Prescription> findByDoctorId(@Param("doctorId") Long doctorId);
}
