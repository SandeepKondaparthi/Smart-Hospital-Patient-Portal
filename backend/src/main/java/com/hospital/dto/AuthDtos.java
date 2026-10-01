package com.hospital.dto;

import com.hospital.enums.Gender;
import com.hospital.enums.Role;
import com.hospital.enums.Specialty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public final class AuthDtos {

    private AuthDtos() {}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].*$",
                message = "Password must contain uppercase, lowercase, digit and special character")
        private String password;

        @NotBlank(message = "First name is required")
        @Size(max = 100)
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        private String lastName;

        @Size(max = 20)
        private String phone;

        @NotNull(message = "Role is required")
        private Role role;

        // Patient-specific
        private LocalDate dateOfBirth;
        private Gender gender;
        private String bloodGroup;
        private String address;
        private String emergencyContactName;
        private String emergencyContactPhone;
        private String insuranceProvider;
        private String insurancePolicyNumber;

        // Doctor-specific
        private Specialty specialty;
        private String licenseNumber;
        private Integer yearsOfExperience;
        private String bio;
        private BigDecimal consultationFee;
        private LocalTime availableFrom;
        private LocalTime availableTo;
        private String department;
        private String qualifications;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private UserInfo user;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String phone;
        private Role role;
        private Long patientId;
        private Long doctorId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest {
        @NotBlank
        private String refreshToken;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForgotPasswordRequest {
        @NotBlank
        @Email
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank
        private String token;

        @NotBlank
        @Size(min = 8)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].*$",
                message = "Password must contain uppercase, lowercase, digit and special character")
        private String newPassword;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;

        @NotBlank
        @Size(min = 8)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].*$",
                message = "Password must contain uppercase, lowercase, digit and special character")
        private String newPassword;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfileRequest {
        @Size(max = 100)
        private String firstName;

        @Size(max = 100)
        private String lastName;

        @Size(max = 20)
        private String phone;

        @Email
        private String email;
    }
}
