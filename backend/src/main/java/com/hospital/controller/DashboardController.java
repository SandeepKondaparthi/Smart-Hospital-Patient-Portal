package com.hospital.controller;

import com.hospital.dto.CommonDtos.DashboardStatsDto;
import com.hospital.entity.User;
import com.hospital.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardStatsDto> getStats(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.getStats(user));
    }
}
