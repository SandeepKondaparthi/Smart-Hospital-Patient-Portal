package com.hospital.service;

import com.hospital.dto.CommonDtos.*;
import com.hospital.entity.Doctor;
import com.hospital.entity.DoctorAvailability;
import com.hospital.enums.Specialty;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.DoctorAvailabilityRepository;
import com.hospital.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<DoctorDto> search(Specialty specialty, Boolean availableOnly) {
        boolean avail = availableOnly != null && availableOnly;
        return doctorRepository.search(specialty, avail).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorDto getById(Long id) {
        Doctor doctor = doctorRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        return toDto(doctor);
    }

    @Transactional(readOnly = true)
    public List<TimeSlotDto> getAvailableSlots(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        DayOfWeek day = date.getDayOfWeek();
        List<DoctorAvailability> availabilities = availabilityRepository
                .findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, day);

        LocalTime from = doctor.getAvailableFrom() != null ? doctor.getAvailableFrom() : LocalTime.of(9, 0);
        LocalTime to = doctor.getAvailableTo() != null ? doctor.getAvailableTo() : LocalTime.of(17, 0);
        int slotMinutes = 30;

        if (!availabilities.isEmpty()) {
            DoctorAvailability av = availabilities.get(0);
            from = av.getStartTime();
            to = av.getEndTime();
            slotMinutes = av.getSlotDurationMinutes();
        }

        List<LocalTime> booked = appointmentRepository
                .findByDoctorIdAndAppointmentDateAndStatusNotIn(
                        doctorId, date,
                        List.of(com.hospital.enums.AppointmentStatus.CANCELLED,
                                com.hospital.enums.AppointmentStatus.NO_SHOW))
                .stream()
                .map(a -> a.getStartTime())
                .toList();

        List<TimeSlotDto> slots = new ArrayList<>();
        LocalTime current = from;
        while (current.plusMinutes(slotMinutes).compareTo(to) <= 0) {
            LocalTime end = current.plusMinutes(slotMinutes);
            boolean available = !booked.contains(current);
            // Don't offer past slots for today
            if (date.equals(LocalDate.now()) && current.isBefore(LocalTime.now())) {
                available = false;
            }
            slots.add(TimeSlotDto.builder()
                    .startTime(current)
                    .endTime(end)
                    .available(available)
                    .build());
            current = end;
        }
        return slots;
    }

    public DoctorDto toDto(Doctor d) {
        return DoctorDto.builder()
                .id(d.getId())
                .userId(d.getUser().getId())
                .firstName(d.getUser().getFirstName())
                .lastName(d.getUser().getLastName())
                .fullName(d.getUser().getFullName())
                .email(d.getUser().getEmail())
                .phone(d.getUser().getPhone())
                .specialty(d.getSpecialty())
                .licenseNumber(d.getLicenseNumber())
                .yearsOfExperience(d.getYearsOfExperience())
                .bio(d.getBio())
                .consultationFee(d.getConsultationFee())
                .availableFrom(d.getAvailableFrom())
                .availableTo(d.getAvailableTo())
                .isAvailable(d.getIsAvailable())
                .maxPatientsPerDay(d.getMaxPatientsPerDay())
                .department(d.getDepartment())
                .qualifications(d.getQualifications())
                .build();
    }
}
