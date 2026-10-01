package com.hospital.service;

import com.hospital.dto.CommonDtos.*;
import com.hospital.entity.*;
import com.hospital.enums.Role;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.UnauthorizedException;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    @Transactional(readOnly = true)
    public List<PrescriptionDto> getPatientPrescriptions(Long patientId, User currentUser) {
        checkAccess(patientId, currentUser);
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PrescriptionDto getById(Long id, User currentUser) {
        Prescription p = prescriptionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));
        checkAccess(p.getPatient().getId(), currentUser);
        return toDto(p);
    }

    @Transactional
    public PrescriptionDto create(User currentUser, CreatePrescriptionRequest request) {
        if (currentUser.getRole() != Role.DOCTOR && currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only doctors can issue prescriptions");
        }

        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        Patient patient = patientRepository.findByIdWithUser(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        MedicalRecord medicalRecord = null;
        if (request.getMedicalRecordId() != null) {
            medicalRecord = medicalRecordRepository.findById(request.getMedicalRecordId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));
        }

        Prescription prescription = Prescription.builder()
                .patient(patient)
                .doctor(doctor)
                .medicalRecord(medicalRecord)
                .prescriptionDate(LocalDate.now())
                .validUntil(request.getValidUntil())
                .notes(request.getNotes())
                .isActive(true)
                .build();

        if (request.getItems() != null) {
            for (PrescriptionItemRequest itemReq : request.getItems()) {
                PrescriptionItem item = PrescriptionItem.builder()
                        .medicationName(itemReq.getMedicationName())
                        .dosage(itemReq.getDosage())
                        .frequency(itemReq.getFrequency())
                        .duration(itemReq.getDuration())
                        .quantity(itemReq.getQuantity())
                        .instructions(itemReq.getInstructions())
                        .route(itemReq.getRoute())
                        .build();
                prescription.addItem(item);
            }
        }

        prescription = prescriptionRepository.save(prescription);
        return toDto(prescriptionRepository.findByIdWithDetails(prescription.getId()).orElse(prescription));
    }

    private void checkAccess(Long patientId, User user) {
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.DOCTOR) return;
        if (user.getRole() == Role.PATIENT) {
            Patient p = patientRepository.findByUserId(user.getId()).orElse(null);
            if (p != null && p.getId().equals(patientId)) return;
        }
        throw new UnauthorizedException("Access denied");
    }

    public PrescriptionDto toDto(Prescription p) {
        List<PrescriptionItemDto> items = p.getItems() != null
                ? p.getItems().stream().map(i -> PrescriptionItemDto.builder()
                .id(i.getId())
                .medicationName(i.getMedicationName())
                .dosage(i.getDosage())
                .frequency(i.getFrequency())
                .duration(i.getDuration())
                .quantity(i.getQuantity())
                .instructions(i.getInstructions())
                .route(i.getRoute())
                .build()).collect(Collectors.toList())
                : List.of();

        return PrescriptionDto.builder()
                .id(p.getId())
                .patientId(p.getPatient().getId())
                .patientName(p.getPatient().getUser().getFullName())
                .doctorId(p.getDoctor().getId())
                .doctorName(p.getDoctor().getUser().getFullName())
                .medicalRecordId(p.getMedicalRecord() != null ? p.getMedicalRecord().getId() : null)
                .prescriptionDate(p.getPrescriptionDate())
                .validUntil(p.getValidUntil())
                .notes(p.getNotes())
                .isActive(p.getIsActive())
                .items(items)
                .build();
    }
}
