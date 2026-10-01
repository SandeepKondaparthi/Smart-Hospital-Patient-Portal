package com.hospital.service;

import com.hospital.dto.AuthDtos.*;
import com.hospital.entity.*;
import com.hospital.enums.Role;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.exception.UnauthorizedException;
import com.hospital.repository.*;
import com.hospital.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new BadRequestException("Email already registered");
        }

        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot self-register as ADMIN");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .isActive(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        Long patientId = null;
        Long doctorId = null;

        if (request.getRole() == Role.PATIENT) {
            Patient patient = Patient.builder()
                    .user(user)
                    .dateOfBirth(request.getDateOfBirth())
                    .gender(request.getGender())
                    .bloodGroup(request.getBloodGroup())
                    .address(request.getAddress())
                    .emergencyContactName(request.getEmergencyContactName())
                    .emergencyContactPhone(request.getEmergencyContactPhone())
                    .insuranceProvider(request.getInsuranceProvider())
                    .insurancePolicyNumber(request.getInsurancePolicyNumber())
                    .build();
            patient = patientRepository.save(patient);
            patientId = patient.getId();
        } else if (request.getRole() == Role.DOCTOR) {
            if (request.getSpecialty() == null || request.getLicenseNumber() == null) {
                throw new BadRequestException("Specialty and license number are required for doctors");
            }
            Doctor doctor = Doctor.builder()
                    .user(user)
                    .specialty(request.getSpecialty())
                    .licenseNumber(request.getLicenseNumber())
                    .yearsOfExperience(request.getYearsOfExperience())
                    .bio(request.getBio())
                    .consultationFee(request.getConsultationFee())
                    .availableFrom(request.getAvailableFrom())
                    .availableTo(request.getAvailableTo())
                    .isAvailable(true)
                    .department(request.getDepartment())
                    .qualifications(request.getQualifications())
                    .build();
            doctor = doctorRepository.save(doctor);
            doctorId = doctor.getId();
        }

        return buildAuthResponse(user, patientId, doctorId);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        Long patientId = null;
        Long doctorId = null;
        if (user.getRole() == Role.PATIENT) {
            patientId = patientRepository.findByUserId(user.getId()).map(Patient::getId).orElse(null);
        } else if (user.getRole() == Role.DOCTOR) {
            doctorId = doctorRepository.findByUserId(user.getId()).map(Doctor::getId).orElse(null);
        }

        return buildAuthResponse(user, patientId, doctorId);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (stored.getRevoked() || stored.getExpiryDate().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = stored.getUser();
        refreshTokenRepository.delete(stored);

        Long patientId = null;
        Long doctorId = null;
        if (user.getRole() == Role.PATIENT) {
            patientId = patientRepository.findByUserId(user.getId()).map(Patient::getId).orElse(null);
        } else if (user.getRole() == Role.DOCTOR) {
            doctorId = doctorRepository.findByUserId(user.getId()).map(Doctor::getId).orElse(null);
        }

        return buildAuthResponse(user, patientId, doctorId);
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(Instant.now().plusSeconds(3600)); // 1 hour
        userRepository.save(user);

        // Attempt to send email; if mail not configured, log the token for dev
        try {
            if (mailFrom != null && !mailFrom.isBlank()) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Password Reset - Hospital Management");
                message.setText("Click the link to reset your password (valid 1 hour):\n\n"
                        + frontendUrl + "/reset-password?token=" + token
                        + "\n\nIf you did not request this, ignore this email.");
                mailSender.send(message);
            } else {
                // Dev fallback: token is still stored; frontend can use it
                System.out.println("[DEV] Password reset token for " + user.getEmail() + ": " + token);
            }
        } catch (Exception e) {
            System.out.println("[DEV] Mail failed. Reset token for " + user.getEmail() + ": " + token);
        }

        return new MessageResponse("If an account exists with that email, a reset link has been sent.", true);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(Instant.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        refreshTokenRepository.deleteByUser(user);

        return new MessageResponse("Password has been reset successfully", true);
    }

    @Transactional
    public MessageResponse changePassword(User currentUser, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        currentUser.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);
        refreshTokenRepository.deleteByUser(currentUser);
        return new MessageResponse("Password changed successfully", true);
    }

    @Transactional
    public UserInfo updateProfile(User currentUser, UpdateProfileRequest request) {
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(currentUser.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
                throw new BadRequestException("Email already in use");
            }
            currentUser.setEmail(request.getEmail().toLowerCase());
        }
        if (request.getFirstName() != null) currentUser.setFirstName(request.getFirstName());
        if (request.getLastName() != null) currentUser.setLastName(request.getLastName());
        if (request.getPhone() != null) currentUser.setPhone(request.getPhone());
        userRepository.save(currentUser);

        Long patientId = patientRepository.findByUserId(currentUser.getId()).map(Patient::getId).orElse(null);
        Long doctorId = doctorRepository.findByUserId(currentUser.getId()).map(Doctor::getId).orElse(null);

        return UserInfo.builder()
                .id(currentUser.getId())
                .email(currentUser.getEmail())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .fullName(currentUser.getFullName())
                .phone(currentUser.getPhone())
                .role(currentUser.getRole())
                .patientId(patientId)
                .doctorId(doctorId)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    private AuthResponse buildAuthResponse(User user, Long patientId, Long doctorId) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .user(UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .role(user.getRole())
                        .patientId(patientId)
                        .doctorId(doctorId)
                        .build())
                .build();
    }

    // Import for MessageResponse
    public record MessageResponse(String message, boolean success) {}
}
