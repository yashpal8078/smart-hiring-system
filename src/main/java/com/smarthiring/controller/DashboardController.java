package com.smarthiring.controller;

import com.smarthiring.dto.response.ApiResponse;
import com.smarthiring.dto.response.DashboardStatsResponse;
import com.smarthiring.security.CustomUserDetails;
import com.smarthiring.service.CandidateService;
import com.smarthiring.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Dashboard Statistics APIs")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CandidateService candidateService;

    /**
     * Get admin dashboard stats
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Admin Dashboard", description = "Get admin dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getAdminDashboard() {
        log.info("Get admin dashboard stats");

        DashboardStatsResponse response = dashboardService.getAdminDashboardStats();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get HR dashboard stats
     */
    @GetMapping("/hr")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Get HR Dashboard", description = "Get HR dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getHrDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Get HR dashboard stats for user: {}", userDetails.getEmail());

        DashboardStatsResponse response = dashboardService.getHrDashboardStats(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get candidate dashboard stats
     */
    @GetMapping("/candidate")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get Candidate Dashboard", description = "Get candidate dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getCandidateDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Get candidate dashboard stats for user: {}", userDetails.getEmail());

        Long candidateId = candidateService.getCandidateByUserId(userDetails.getId()).getId();
        DashboardStatsResponse response = dashboardService.getCandidateDashboardStats(candidateId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}