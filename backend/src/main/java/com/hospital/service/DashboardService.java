package com.hospital.service;

import com.hospital.dto.CommonDtos.*;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.User;
import com.hospital.enums.Role;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillingRepository billingRepository;
    private final AppointmentService appointmentService;

    @Transactional(readOnly = true)
    public DashboardStatsDto getStats(User currentUser) {
        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();
        long todayAppointments = 0;
        long upcoming = 0;
        long pendingBills = billingRepository.countOutstanding();
        BigDecimal outstanding = BigDecimal.ZERO;
        List<AppointmentDto> recent = Collections.emptyList();
        List<AppointmentDto> todaySchedule = Collections.emptyList();

        if (currentUser.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserId(currentUser.getId()).orElse(null);
            if (doctor != null) {
                todaySchedule = appointmentRepository.findByDoctorIdAndDate(doctor.getId(), LocalDate.now())
                        .stream().map(appointmentService::toDto).collect(Collectors.toList());
                todayAppointments = todaySchedule.size();
                upcoming = appointmentRepository.findUpcomingByDoctor(doctor.getId(), LocalDate.now())
                        .stream().filter(a -> a.getAppointmentDate().isAfter(LocalDate.now())).count();
                recent = todaySchedule.stream().limit(5).collect(Collectors.toList());
            }
        } else if (currentUser.getRole() == Role.PATIENT) {
            Patient patient = patientRepository.findByUserId(currentUser.getId()).orElse(null);
            if (patient != null) {
                recent = appointmentRepository.findByPatientId(patient.getId()).stream()
                        .limit(5).map(appointmentService::toDto).collect(Collectors.toList());
                upcoming = recent.stream()
                        .filter(a -> a.getAppointmentDate() != null
                                && !a.getAppointmentDate().isBefore(LocalDate.now())
                                && a.getStatus() != com.hospital.enums.AppointmentStatus.CANCELLED
                                && a.getStatus() != com.hospital.enums.AppointmentStatus.COMPLETED)
                        .count();
            }
        } else {
            // Admin / Receptionist overview
            todaySchedule = appointmentRepository.findAll().stream()
                    .filter(a -> a.getAppointmentDate().equals(LocalDate.now()))
                    .limit(10)
                    .map(appointmentService::toDto)
                    .collect(Collectors.toList());
            todayAppointments = todaySchedule.size();
            recent = todaySchedule;
        }

        return DashboardStatsDto.builder()
                .totalPatients(totalPatients)
                .totalDoctors(totalDoctors)
                .todayAppointments(todayAppointments)
                .upcomingAppointments(upcoming)
                .pendingBills(pendingBills)
                .outstandingAmount(outstanding)
                .recentAppointments(recent)
                .todaySchedule(todaySchedule)
                .build();
    }
}
