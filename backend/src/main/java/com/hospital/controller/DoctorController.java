package com.hospital.controller;

import com.hospital.dto.CommonDtos.*;
import com.hospital.enums.Specialty;
import com.hospital.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorDto>> search(
            @RequestParam(required = false) Specialty specialty,
            @RequestParam(required = false, defaultValue = "false") Boolean availableOnly) {
        return ResponseEntity.ok(doctorService.search(specialty, availableOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getById(id));
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<List<TimeSlotDto>> getSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(doctorService.getAvailableSlots(id, date));
    }

    @GetMapping("/specialties")
    public ResponseEntity<Specialty[]> getSpecialties() {
        return ResponseEntity.ok(Specialty.values());
    }
}
