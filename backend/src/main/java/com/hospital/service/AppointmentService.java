package com.hospital.service;

import com.hospital.dto.CommonDtos.*;
import com.hospital.entity.*;
import com.hospital.enums.AppointmentStatus;
import com.hospital.enums.Role;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.UnauthorizedException;
import com.hospital.repository.*;
import com.hospital.websocket.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final NotificationService notificationService;

    @Transactional
    public AppointmentDto book(User currentUser, BookAppointmentRequest request) {
        Patient patient;
        if (currentUser.getRole() == Role.PATIENT) {
            patient = patientRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
        } else {
            throw new BadRequestException("Only patients can book appointments via this endpoint. Staff should use admin booking.");
        }

        Doctor doctor = doctorRepository.findByIdWithUser(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (!Boolean.TRUE.equals(doctor.getIsAvailable())) {
            throw new BadRequestException("Doctor is not available for booking");
        }

        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot book appointments in the past");
        }

        appointmentRepository.findConflictingSlot(doctor.getId(), request.getAppointmentDate(), request.getStartTime())
                .ifPresent(a -> {
                    throw new BadRequestException("This time slot is already booked");
                });

        long count = appointmentRepository.countByDoctorAndDate(doctor.getId(), request.getAppointmentDate());
        if (doctor.getMaxPatientsPerDay() != null && count >= doctor.getMaxPatientsPerDay()) {
            throw new BadRequestException("Doctor has reached maximum appointments for this day");
        }

        LocalTime endTime = request.getStartTime().plusMinutes(30);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .status(AppointmentStatus.SCHEDULED)
                .reason(request.getReason())
                .createdBy(currentUser)
                .build();

        appointment = appointmentRepository.save(appointment);

        AppointmentDto dto = toDto(appointment);
        notificationService.notifyAppointmentBooked(dto);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getMyAppointments(User currentUser) {
        if (currentUser.getRole() == Role.PATIENT) {
            Patient patient = patientRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
            return appointmentRepository.findByPatientId(patient.getId()).stream()
                    .map(this::toDto).collect(Collectors.toList());
        } else if (currentUser.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
            return appointmentRepository.findUpcomingByDoctor(doctor.getId(), LocalDate.now().minusDays(7))
                    .stream().map(this::toDto).collect(Collectors.toList());
        }
        throw new UnauthorizedException("Not authorized");
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getDoctorSchedule(User currentUser, LocalDate date) {
        Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        LocalDate target = date != null ? date : LocalDate.now();
        return appointmentRepository.findByDoctorIdAndDate(doctor.getId(), target).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentDto getById(Long id, User currentUser) {
        Appointment a = appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        checkAccess(a, currentUser);
        return toDto(a);
    }

    @Transactional
    public AppointmentDto updateStatus(Long id, AppointmentStatus status, String notes, User currentUser) {
        Appointment a = appointmentRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        checkAccess(a, currentUser);

        a.setStatus(status);
        if (notes != null) {
            if (status == AppointmentStatus.CANCELLED) {
                a.setCancellationReason(notes);
            } else {
                a.setNotes(notes);
            }
        }
        a = appointmentRepository.save(a);
        AppointmentDto dto = toDto(a);
        notificationService.notifyAppointmentUpdated(dto);
        return dto;
    }

    private void checkAccess(Appointment a, User user) {
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.RECEPTIONIST) return;
        if (user.getRole() == Role.PATIENT) {
            Patient p = patientRepository.findByUserId(user.getId()).orElse(null);
            if (p != null && p.getId().equals(a.getPatient().getId())) return;
        }
        if (user.getRole() == Role.DOCTOR) {
            Doctor d = doctorRepository.findByUserId(user.getId()).orElse(null);
            if (d != null && d.getId().equals(a.getDoctor().getId())) return;
        }
        throw new UnauthorizedException("Access denied to this appointment");
    }

    public AppointmentDto toDto(Appointment a) {
        return AppointmentDto.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getUser().getFullName())
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getUser().getFullName())
                .doctorSpecialty(a.getDoctor().getSpecialty())
                .appointmentDate(a.getAppointmentDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .status(a.getStatus())
                .reason(a.getReason())
                .notes(a.getNotes())
                .cancellationReason(a.getCancellationReason())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
