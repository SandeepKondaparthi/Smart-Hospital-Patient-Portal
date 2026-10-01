package com.hospital.controller;

import com.hospital.dto.CommonDtos.*;
import com.hospital.entity.User;
import com.hospital.enums.AppointmentStatus;
import com.hospital.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentDto> book(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BookAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.book(user, request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<AppointmentDto>> myAppointments(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(user));
    }

    @GetMapping("/schedule")
    public ResponseEntity<List<AppointmentDto>> doctorSchedule(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getDoctorSchedule(user, date));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDto> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(appointmentService.getById(id, user));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentDto> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User user) {
        AppointmentStatus status = AppointmentStatus.valueOf(body.get("status"));
        String notes = body.get("notes");
        return ResponseEntity.ok(appointmentService.updateStatus(id, status, notes, user));
    }
}
