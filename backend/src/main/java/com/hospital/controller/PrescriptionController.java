package com.hospital.controller;

import com.hospital.dto.CommonDtos.*;
import com.hospital.entity.User;
import com.hospital.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionDto>> getPatientPrescriptions(
            @PathVariable Long patientId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(prescriptionService.getPatientPrescriptions(patientId, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionDto> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(prescriptionService.getById(id, user));
    }

    @PostMapping
    public ResponseEntity<PrescriptionDto> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreatePrescriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prescriptionService.create(user, request));
    }
}
