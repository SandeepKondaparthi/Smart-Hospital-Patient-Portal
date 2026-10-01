package com.hospital.entity;

import com.hospital.enums.BillingStatus;
import com.hospital.enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "billings", indexes = {
        @Index(name = "idx_billings_patient", columnList = "patient_id"),
        @Index(name = "idx_billings_status", columnList = "status"),
        @Index(name = "idx_billings_invoice", columnList = "invoice_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Billing extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(name = "billing_date", nullable = false)
    private LocalDate billingDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "amount_paid", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BillingStatus status = BillingStatus.PENDING;

    @Column(name = "insurance_provider", length = 150)
    private String insuranceProvider;

    @Column(name = "insurance_policy_number", length = 100)
    private String insurancePolicyNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_status", length = 30)
    @Builder.Default
    private ClaimStatus claimStatus = ClaimStatus.NOT_SUBMITTED;

    @Column(name = "claim_number", length = 100)
    private String claimNumber;

    @Column(name = "claim_amount", precision = 12, scale = 2)
    private BigDecimal claimAmount;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "billing", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BillingItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "billing", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    public void addItem(BillingItem item) {
        items.add(item);
        item.setBilling(this);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setBilling(this);
    }
}
