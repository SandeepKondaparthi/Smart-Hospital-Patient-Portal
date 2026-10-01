package com.hospital.repository;

import com.hospital.entity.Billing;
import com.hospital.enums.BillingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {

    @Query("SELECT b FROM Billing b JOIN FETCH b.patient p JOIN FETCH p.user " +
           "LEFT JOIN FETCH b.items LEFT JOIN FETCH b.payments " +
           "WHERE b.patient.id = :patientId ORDER BY b.billingDate DESC")
    List<Billing> findByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT b FROM Billing b JOIN FETCH b.patient p JOIN FETCH p.user " +
           "LEFT JOIN FETCH b.items LEFT JOIN FETCH b.payments WHERE b.id = :id")
    Optional<Billing> findByIdWithDetails(@Param("id") Long id);

    Optional<Billing> findByInvoiceNumber(String invoiceNumber);

    List<Billing> findByStatus(BillingStatus status);

    @Query("SELECT COUNT(b) FROM Billing b WHERE b.status IN ('PENDING', 'PARTIALLY_PAID', 'OVERDUE')")
    long countOutstanding();
}
