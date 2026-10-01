package com.hospital.dto;

import com.hospital.enums.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class CommonDtos {

    private CommonDtos() {}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorDto {
        private Long id;
        private Long userId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone;
        private Specialty specialty;
        private String licenseNumber;
        private Integer yearsOfExperience;
        private String bio;
        private BigDecimal consultationFee;
        private LocalTime availableFrom;
        private LocalTime availableTo;
        private Boolean isAvailable;
        private Integer maxPatientsPerDay;
        private String department;
        private String qualifications;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientDto {
        private Long id;
        private Long userId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone;
        private LocalDate dateOfBirth;
        private Gender gender;
        private String bloodGroup;
        private String address;
        private String emergencyContactName;
        private String emergencyContactPhone;
        private String insuranceProvider;
        private String insurancePolicyNumber;
        private String allergies;
        private String chronicConditions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppointmentDto {
        private Long id;
        private Long patientId;
        private String patientName;
        private Long doctorId;
        private String doctorName;
        private Specialty doctorSpecialty;
        private LocalDate appointmentDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private AppointmentStatus status;
        private String reason;
        private String notes;
        private String cancellationReason;
        private Instant createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookAppointmentRequest {
        private Long doctorId;
        private LocalDate appointmentDate;
        private LocalTime startTime;
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotDto {
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean available;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicalRecordDto {
        private Long id;
        private Long patientId;
        private String patientName;
        private Long doctorId;
        private String doctorName;
        private Long appointmentId;
        private LocalDate visitDate;
        private String chiefComplaint;
        private String diagnosis;
        private String diagnosisCode;
        private String symptoms;
        private String vitalSigns;
        private String examinationNotes;
        private String treatmentPlan;
        private LocalDate followUpDate;
        private String followUpNotes;
        private Instant createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateMedicalRecordRequest {
        private Long patientId;
        private Long appointmentId;
        private LocalDate visitDate;
        private String chiefComplaint;
        private String diagnosis;
        private String diagnosisCode;
        private String symptoms;
        private String vitalSigns;
        private String examinationNotes;
        private String treatmentPlan;
        private LocalDate followUpDate;
        private String followUpNotes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LabResultDto {
        private Long id;
        private Long patientId;
        private Long medicalRecordId;
        private String orderedByName;
        private String testName;
        private String testCode;
        private String resultValue;
        private String unit;
        private String referenceRange;
        private Boolean isAbnormal;
        private LocalDate testDate;
        private LocalDate resultDate;
        private String notes;
        private String labName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionDto {
        private Long id;
        private Long patientId;
        private String patientName;
        private Long doctorId;
        private String doctorName;
        private Long medicalRecordId;
        private LocalDate prescriptionDate;
        private LocalDate validUntil;
        private String notes;
        private Boolean isActive;
        private List<PrescriptionItemDto> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionItemDto {
        private Long id;
        private String medicationName;
        private String dosage;
        private String frequency;
        private String duration;
        private Integer quantity;
        private String instructions;
        private String route;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePrescriptionRequest {
        private Long patientId;
        private Long medicalRecordId;
        private LocalDate validUntil;
        private String notes;
        private List<PrescriptionItemRequest> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionItemRequest {
        private String medicationName;
        private String dosage;
        private String frequency;
        private String duration;
        private Integer quantity;
        private String instructions;
        private String route;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingDto {
        private Long id;
        private Long patientId;
        private String patientName;
        private Long appointmentId;
        private String invoiceNumber;
        private LocalDate billingDate;
        private LocalDate dueDate;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private BigDecimal amountPaid;
        private BillingStatus status;
        private String insuranceProvider;
        private String insurancePolicyNumber;
        private ClaimStatus claimStatus;
        private String claimNumber;
        private BigDecimal claimAmount;
        private String notes;
        private List<BillingItemDto> items;
        private List<PaymentDto> payments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingItemDto {
        private Long id;
        private String description;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String serviceCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDto {
        private Long id;
        private BigDecimal amount;
        private String paymentMethod;
        private String transactionId;
        private LocalDateTime paymentDate;
        private String notes;
        private String receivedByName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBillingRequest {
        private Long patientId;
        private Long appointmentId;
        private LocalDate dueDate;
        private BigDecimal taxAmount;
        private BigDecimal discountAmount;
        private String insuranceProvider;
        private String insurancePolicyNumber;
        private String notes;
        private List<BillingItemRequest> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillingItemRequest {
        private String description;
        private Integer quantity;
        private BigDecimal unitPrice;
        private String serviceCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordPaymentRequest {
        private BigDecimal amount;
        private String paymentMethod;
        private String transactionId;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateClaimRequest {
        private ClaimStatus claimStatus;
        private String claimNumber;
        private BigDecimal claimAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStatsDto {
        private long totalPatients;
        private long totalDoctors;
        private long todayAppointments;
        private long upcomingAppointments;
        private long pendingBills;
        private BigDecimal outstandingAmount;
        private List<AppointmentDto> recentAppointments;
        private List<AppointmentDto> todaySchedule;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private String message;
        private boolean success;
    }
}
