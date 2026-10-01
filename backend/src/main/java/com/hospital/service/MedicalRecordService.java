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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final LabResultRepository labResultRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<MedicalRecordDto> getPatientRecords(Long patientId, User currentUser) {
        checkPatientAccess(patientId, currentUser);
        return medicalRecordRepository.findByPatientId(patientId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedicalRecordDto getById(Long id, User currentUser) {
        MedicalRecord m = medicalRecordRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));
        checkPatientAccess(m.getPatient().getId(), currentUser);
        return toDto(m);
    }

    @Transactional
    public MedicalRecordDto create(User currentUser, CreateMedicalRecordRequest request) {
        if (currentUser.getRole() != Role.DOCTOR && currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only doctors can create medical records");
        }

        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        Patient patient = patientRepository.findByIdWithUser(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Appointment appointment = null;
        if (request.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        }

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .visitDate(request.getVisitDate())
                .chiefComplaint(request.getChiefComplaint())
                .diagnosis(request.getDiagnosis())
                .diagnosisCode(request.getDiagnosisCode())
                .symptoms(request.getSymptoms())
                .vitalSigns(request.getVitalSigns())
                .examinationNotes(request.getExaminationNotes())
                .treatmentPlan(request.getTreatmentPlan())
                .followUpDate(request.getFollowUpDate())
                .followUpNotes(request.getFollowUpNotes())
                .build();

        record = medicalRecordRepository.save(record);
        return toDto(record);
    }

    @Transactional(readOnly = true)
    public List<LabResultDto> getLabResults(Long patientId, User currentUser) {
        checkPatientAccess(patientId, currentUser);
        return labResultRepository.findByPatientId(patientId).stream()
                .map(this::toLabDto).collect(Collectors.toList());
    }

    private void checkPatientAccess(Long patientId, User user) {
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.DOCTOR || user.getRole() == Role.RECEPTIONIST) {
            return;
        }
        if (user.getRole() == Role.PATIENT) {
            Patient p = patientRepository.findByUserId(user.getId()).orElse(null);
            if (p != null && p.getId().equals(patientId)) return;
        }
        throw new UnauthorizedException("Access denied to medical records");
    }

    public MedicalRecordDto toDto(MedicalRecord m) {
        return MedicalRecordDto.builder()
                .id(m.getId())
                .patientId(m.getPatient().getId())
                .patientName(m.getPatient().getUser().getFullName())
                .doctorId(m.getDoctor().getId())
                .doctorName(m.getDoctor().getUser().getFullName())
                .appointmentId(m.getAppointment() != null ? m.getAppointment().getId() : null)
                .visitDate(m.getVisitDate())
                .chiefComplaint(m.getChiefComplaint())
                .diagnosis(m.getDiagnosis())
                .diagnosisCode(m.getDiagnosisCode())
                .symptoms(m.getSymptoms())
                .vitalSigns(m.getVitalSigns())
                .examinationNotes(m.getExaminationNotes())
                .treatmentPlan(m.getTreatmentPlan())
                .followUpDate(m.getFollowUpDate())
                .followUpNotes(m.getFollowUpNotes())
                .createdAt(m.getCreatedAt())
                .build();
    }

    public LabResultDto toLabDto(LabResult l) {
        return LabResultDto.builder()
                .id(l.getId())
                .patientId(l.getPatient().getId())
                .medicalRecordId(l.getMedicalRecord() != null ? l.getMedicalRecord().getId() : null)
                .orderedByName(l.getOrderedBy() != null ? l.getOrderedBy().getUser().getFullName() : null)
                .testName(l.getTestName())
                .testCode(l.getTestCode())
                .resultValue(l.getResultValue())
                .unit(l.getUnit())
                .referenceRange(l.getReferenceRange())
                .isAbnormal(l.getIsAbnormal())
                .testDate(l.getTestDate())
                .resultDate(l.getResultDate())
                .notes(l.getNotes())
                .labName(l.getLabName())
                .build();
    }
}
