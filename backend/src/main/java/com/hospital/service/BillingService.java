package com.hospital.service;

import com.hospital.dto.CommonDtos.*;
import com.hospital.entity.*;
import com.hospital.enums.BillingStatus;
import com.hospital.enums.ClaimStatus;
import com.hospital.enums.Role;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.UnauthorizedException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillingRepository billingRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private static final AtomicLong invoiceSeq = new AtomicLong(System.currentTimeMillis() % 100000);

    @Transactional(readOnly = true)
    public List<BillingDto> getPatientBillings(Long patientId, User currentUser) {
        checkAccess(patientId, currentUser);
        return billingRepository.findByPatientId(patientId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BillingDto getById(Long id, User currentUser) {
        Billing b = billingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing not found"));
        checkAccess(b.getPatient().getId(), currentUser);
        return toDto(b);
    }

    @Transactional
    public BillingDto create(User currentUser, CreateBillingRequest request) {
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.RECEPTIONIST
                && currentUser.getRole() != Role.DOCTOR) {
            throw new UnauthorizedException("Not authorized to create bills");
        }

        Patient patient = patientRepository.findByIdWithUser(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId()).orElse(null);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (BillingItemRequest item : request.getItems()) {
                BigDecimal qty = BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 1);
                subtotal = subtotal.add(item.getUnitPrice().multiply(qty));
            }
        }

        BigDecimal tax = request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(tax).subtract(discount);

        String invoiceNumber = "INV-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + invoiceSeq.incrementAndGet();

        Billing billing = Billing.builder()
                .patient(patient)
                .appointment(appointment)
                .invoiceNumber(invoiceNumber)
                .billingDate(LocalDate.now())
                .dueDate(request.getDueDate() != null ? request.getDueDate() : LocalDate.now().plusDays(30))
                .subtotal(subtotal)
                .taxAmount(tax)
                .discountAmount(discount)
                .totalAmount(total)
                .amountPaid(BigDecimal.ZERO)
                .status(BillingStatus.PENDING)
                .insuranceProvider(request.getInsuranceProvider())
                .insurancePolicyNumber(request.getInsurancePolicyNumber())
                .claimStatus(ClaimStatus.NOT_SUBMITTED)
                .notes(request.getNotes())
                .build();

        if (request.getItems() != null) {
            for (BillingItemRequest itemReq : request.getItems()) {
                int qty = itemReq.getQuantity() != null ? itemReq.getQuantity() : 1;
                BigDecimal lineTotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(qty));
                BillingItem item = BillingItem.builder()
                        .description(itemReq.getDescription())
                        .quantity(qty)
                        .unitPrice(itemReq.getUnitPrice())
                        .totalPrice(lineTotal)
                        .serviceCode(itemReq.getServiceCode())
                        .build();
                billing.addItem(item);
            }
        }

        billing = billingRepository.save(billing);
        return toDto(billingRepository.findByIdWithDetails(billing.getId()).orElse(billing));
    }

    @Transactional
    public BillingDto recordPayment(Long billingId, User currentUser, RecordPaymentRequest request) {
        Billing billing = billingRepository.findByIdWithDetails(billingId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing not found"));

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be positive");
        }

        BigDecimal remaining = billing.getTotalAmount().subtract(billing.getAmountPaid());
        if (request.getAmount().compareTo(remaining) > 0) {
            throw new BadRequestException("Payment exceeds remaining balance");
        }

        Payment payment = Payment.builder()
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getTransactionId())
                .paymentDate(LocalDateTime.now())
                .notes(request.getNotes())
                .receivedBy(currentUser)
                .build();
        billing.addPayment(payment);

        BigDecimal newPaid = billing.getAmountPaid().add(request.getAmount());
        billing.setAmountPaid(newPaid);

        if (newPaid.compareTo(billing.getTotalAmount()) >= 0) {
            billing.setStatus(BillingStatus.PAID);
        } else if (newPaid.compareTo(BigDecimal.ZERO) > 0) {
            billing.setStatus(BillingStatus.PARTIALLY_PAID);
        }

        billing = billingRepository.save(billing);
        return toDto(billing);
    }

    @Transactional
    public BillingDto updateClaim(Long billingId, UpdateClaimRequest request) {
        Billing billing = billingRepository.findByIdWithDetails(billingId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing not found"));
        if (request.getClaimStatus() != null) billing.setClaimStatus(request.getClaimStatus());
        if (request.getClaimNumber() != null) billing.setClaimNumber(request.getClaimNumber());
        if (request.getClaimAmount() != null) billing.setClaimAmount(request.getClaimAmount());
        billing = billingRepository.save(billing);
        return toDto(billing);
    }

    private void checkAccess(Long patientId, User user) {
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.RECEPTIONIST || user.getRole() == Role.DOCTOR) {
            return;
        }
        if (user.getRole() == Role.PATIENT) {
            Patient p = patientRepository.findByUserId(user.getId()).orElse(null);
            if (p != null && p.getId().equals(patientId)) return;
        }
        throw new UnauthorizedException("Access denied to billing");
    }

    public BillingDto toDto(Billing b) {
        List<BillingItemDto> items = b.getItems() != null
                ? b.getItems().stream().map(i -> BillingItemDto.builder()
                .id(i.getId())
                .description(i.getDescription())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .totalPrice(i.getTotalPrice())
                .serviceCode(i.getServiceCode())
                .build()).collect(Collectors.toList())
                : List.of();

        List<PaymentDto> payments = b.getPayments() != null
                ? b.getPayments().stream().map(p -> PaymentDto.builder()
                .id(p.getId())
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .transactionId(p.getTransactionId())
                .paymentDate(p.getPaymentDate())
                .notes(p.getNotes())
                .receivedByName(p.getReceivedBy() != null ? p.getReceivedBy().getFullName() : null)
                .build()).collect(Collectors.toList())
                : List.of();

        return BillingDto.builder()
                .id(b.getId())
                .patientId(b.getPatient().getId())
                .patientName(b.getPatient().getUser().getFullName())
                .appointmentId(b.getAppointment() != null ? b.getAppointment().getId() : null)
                .invoiceNumber(b.getInvoiceNumber())
                .billingDate(b.getBillingDate())
                .dueDate(b.getDueDate())
                .subtotal(b.getSubtotal())
                .taxAmount(b.getTaxAmount())
                .discountAmount(b.getDiscountAmount())
                .totalAmount(b.getTotalAmount())
                .amountPaid(b.getAmountPaid())
                .status(b.getStatus())
                .insuranceProvider(b.getInsuranceProvider())
                .insurancePolicyNumber(b.getInsurancePolicyNumber())
                .claimStatus(b.getClaimStatus())
                .claimNumber(b.getClaimNumber())
                .claimAmount(b.getClaimAmount())
                .notes(b.getNotes())
                .items(items)
                .payments(payments)
                .build();
    }
}
