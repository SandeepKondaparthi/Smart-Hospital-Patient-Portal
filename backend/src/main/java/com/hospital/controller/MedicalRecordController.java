package com.hospital.controller;

import com.hospital.dto.CommonDtos.*;
import com.hospital.entity.User;
import com.hospital.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordDto>> getPatientRecords(
            @PathVariable Long patientId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(medicalRecordService.getPatientRecords(patientId, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordDto> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(medicalRecordService.getById(id, user));
    }

    @PostMapping
    public ResponseEntity<MedicalRecordDto> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateMedicalRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalRecordService.create(user, request));
    }

    @GetMapping("/patient/{patientId}/lab-results")
    public ResponseEntity<List<LabResultDto>> getLabResults(
            @PathVariable Long patientId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(medicalRecordService.getLabResults(patientId, user));
    }
}
