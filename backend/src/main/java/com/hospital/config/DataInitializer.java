package com.hospital.config;

import com.hospital.entity.*;
import com.hospital.enums.*;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping...");
            return;
        }

        log.info("Seeding initial data...");
        String password = passwordEncoder.encode("Password@123");

        // Admin
        User admin = userRepository.save(User.builder()
                .email("admin@hospital.com")
                .passwordHash(password)
                .firstName("System")
                .lastName("Admin")
                .phone("+10000000001")
                .role(Role.ADMIN)
                .isActive(true)
                .emailVerified(true)
                .build());

        // Doctors
        User drSmith = userRepository.save(User.builder()
                .email("dr.smith@hospital.com")
                .passwordHash(password)
                .firstName("John")
                .lastName("Smith")
                .phone("+10000000002")
                .role(Role.DOCTOR)
                .isActive(true)
                .emailVerified(true)
                .build());

        Doctor doctorSmith = doctorRepository.save(Doctor.builder()
                .user(drSmith)
                .specialty(Specialty.CARDIOLOGY)
                .licenseNumber("MD-CARD-001")
                .yearsOfExperience(15)
                .bio("Board-certified cardiologist with expertise in interventional cardiology.")
                .consultationFee(new BigDecimal("150.00"))
                .availableFrom(LocalTime.of(9, 0))
                .availableTo(LocalTime.of(17, 0))
                .isAvailable(true)
                .maxPatientsPerDay(12)
                .department("Cardiology")
                .qualifications("MD, FACC")
                .build());

        User drJones = userRepository.save(User.builder()
                .email("dr.jones@hospital.com")
                .passwordHash(password)
                .firstName("Emily")
                .lastName("Jones")
                .phone("+10000000003")
                .role(Role.DOCTOR)
                .isActive(true)
                .emailVerified(true)
                .build());

        Doctor doctorJones = doctorRepository.save(Doctor.builder()
                .user(drJones)
                .specialty(Specialty.PEDIATRICS)
                .licenseNumber("MD-PED-002")
                .yearsOfExperience(10)
                .bio("Pediatrician specializing in childhood development and infectious diseases.")
                .consultationFee(new BigDecimal("120.00"))
                .availableFrom(LocalTime.of(8, 30))
                .availableTo(LocalTime.of(16, 30))
                .isAvailable(true)
                .maxPatientsPerDay(15)
                .department("Pediatrics")
                .qualifications("MD, FAAP")
                .build());

        User drPatel = userRepository.save(User.builder()
                .email("dr.patel@hospital.com")
                .passwordHash(password)
                .firstName("Raj")
                .lastName("Patel")
                .phone("+10000000004")
                .role(Role.DOCTOR)
                .isActive(true)
                .emailVerified(true)
                .build());

        Doctor doctorPatel = doctorRepository.save(Doctor.builder()
                .user(drPatel)
                .specialty(Specialty.GENERAL_PRACTICE)
                .licenseNumber("MD-GP-003")
                .yearsOfExperience(8)
                .bio("Family physician providing comprehensive primary care.")
                .consultationFee(new BigDecimal("80.00"))
                .availableFrom(LocalTime.of(9, 0))
                .availableTo(LocalTime.of(18, 0))
                .isAvailable(true)
                .maxPatientsPerDay(20)
                .department("General Medicine")
                .qualifications("MD, MBBS")
                .build());

        // Availability Mon-Fri for each doctor
        for (Doctor d : new Doctor[]{doctorSmith, doctorJones, doctorPatel}) {
            for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
                availabilityRepository.save(DoctorAvailability.builder()
                        .doctor(d)
                        .dayOfWeek(day)
                        .startTime(d.getAvailableFrom())
                        .endTime(d.getAvailableTo())
                        .slotDurationMinutes(30)
                        .isActive(true)
                        .build());
            }
        }

        // Patients
        User alice = userRepository.save(User.builder()
                .email("patient1@example.com")
                .passwordHash(password)
                .firstName("Alice")
                .lastName("Williams")
                .phone("+10000000005")
                .role(Role.PATIENT)
                .isActive(true)
                .emailVerified(true)
                .build());

        patientRepository.save(Patient.builder()
                .user(alice)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.FEMALE)
                .bloodGroup("A+")
                .address("123 Main St, Springfield")
                .emergencyContactName("Tom Williams")
                .emergencyContactPhone("+10000000015")
                .insuranceProvider("BlueCross")
                .insurancePolicyNumber("BC-123456")
                .allergies("Penicillin")
                .chronicConditions("None")
                .build());

        User bob = userRepository.save(User.builder()
                .email("patient2@example.com")
                .passwordHash(password)
                .firstName("Bob")
                .lastName("Brown")
                .phone("+10000000006")
                .role(Role.PATIENT)
                .isActive(true)
                .emailVerified(true)
                .build());

        patientRepository.save(Patient.builder()
                .user(bob)
                .dateOfBirth(LocalDate.of(1985, 11, 22))
                .gender(Gender.MALE)
                .bloodGroup("O+")
                .address("456 Oak Ave, Springfield")
                .emergencyContactName("Jane Brown")
                .emergencyContactPhone("+10000000016")
                .insuranceProvider("Aetna")
                .insurancePolicyNumber("AE-789012")
                .allergies("None")
                .chronicConditions("Hypertension")
                .build());

        // Receptionist
        userRepository.save(User.builder()
                .email("reception@hospital.com")
                .passwordHash(password)
                .firstName("Sarah")
                .lastName("Lee")
                .phone("+10000000007")
                .role(Role.RECEPTIONIST)
                .isActive(true)
                .emailVerified(true)
                .build());

        log.info("Seed data created. Default password for all users: Password@123");
        log.info("Accounts: admin@hospital.com, dr.smith@hospital.com, dr.jones@hospital.com, " +
                "dr.patel@hospital.com, patient1@example.com, patient2@example.com, reception@hospital.com");
    }
}
