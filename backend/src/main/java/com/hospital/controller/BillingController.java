package com.hospital.controller;

import com.hospital.dto.CommonDtos.*;
import com.hospital.entity.User;
import com.hospital.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billings")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<BillingDto>> getPatientBillings(
            @PathVariable Long patientId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(billingService.getPatientBillings(patientId, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillingDto> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(billingService.getById(id, user));
    }

    @PostMapping
    public ResponseEntity<BillingDto> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateBillingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billingService.create(user, request));
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<BillingDto> recordPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody RecordPaymentRequest request) {
        return ResponseEntity.ok(billingService.recordPayment(id, user, request));
    }

    @PatchMapping("/{id}/claim")
    public ResponseEntity<BillingDto> updateClaim(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClaimRequest request) {
        return ResponseEntity.ok(billingService.updateClaim(id, request));
    }
}
