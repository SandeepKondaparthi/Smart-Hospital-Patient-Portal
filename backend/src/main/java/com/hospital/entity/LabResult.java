package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "lab_results", indexes = {
        @Index(name = "idx_lab_results_patient", columnList = "patient_id"),
        @Index(name = "idx_lab_results_medical_record", columnList = "medical_record_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordered_by")
    private Doctor orderedBy;

    @Column(name = "test_name", nullable = false, length = 200)
    private String testName;

    @Column(name = "test_code", length = 50)
    private String testCode;

    @Column(name = "result_value", length = 100)
    private String resultValue;

    @Column(name = "unit", length = 50)
    private String unit;

    @Column(name = "reference_range", length = 100)
    private String referenceRange;

    @Column(name = "is_abnormal")
    @Builder.Default
    private Boolean isAbnormal = false;

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;

    @Column(name = "result_date")
    private LocalDate resultDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "lab_name", length = 150)
    private String labName;
}
