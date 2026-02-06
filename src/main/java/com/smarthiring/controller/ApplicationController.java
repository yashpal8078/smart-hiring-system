package com.smarthiring.controller;

import com.smarthiring.dto.request.ApplicationRequest;
import com.smarthiring.dto.request.HrReviewRequest;
import com.smarthiring.dto.request.UpdateApplicationStatusRequest;
import com.smarthiring.dto.response.ApiResponse;
import com.smarthiring.dto.response.ApplicationResponse;
import com.smarthiring.dto.response.PagedResponse;
import com.smarthiring.security.CustomUserDetails;
import com.smarthiring.service.ApplicationService;
import com.smarthiring.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Applications", description = "Job Application APIs")
@SecurityRequirement(name = "bearerAuth")
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * Apply for a job (Candidate only)
     */
    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Apply for Job", description = "Submit application for a job (Candidate only)")
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyForJob(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ApplicationRequest request
    ) {
        log.info("Apply for job {} by user: {}", request.getJobId(), userDetails.getEmail());

        ApplicationResponse response = applicationService.applyForJob(request, userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully", response));
    }

    /**
     * Get my applications (Candidate only)
     */
    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get My Applications", description = "Get all applications by current candidate")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        log.info("Get my applications for user: {}", userDetails.getEmail());

        PagedResponse<ApplicationResponse> response = applicationService.getApplicationsByCandidate(
                userDetails.getId(), page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get application by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Application by ID", description = "Get application details by ID")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(
            @PathVariable Long id
    ) {
        log.info("Get application by ID: {}", id);

        ApplicationResponse response = applicationService.getApplicationById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get applications for a job (HR/Admin only)
     */
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Get Applications for Job", description = "Get all applications for a job (HR/Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationResponse>>> getApplicationsByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy
    ) {
        log.info("Get applications for job: {}", jobId);

        PagedResponse<ApplicationResponse> response = applicationService.getApplicationsByJob(
                jobId, page, size, sortBy);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get applications ranked by AI score (HR/Admin only)
     */
    @GetMapping("/job/{jobId}/ranked")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Get Ranked Applications", description = "Get applications sorted by AI score (HR/Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationResponse>>> getRankedApplications(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        log.info("Get ranked applications for job: {}", jobId);

        PagedResponse<ApplicationResponse> response = applicationService.getApplicationsByAiScore(
                jobId, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update application status (HR/Admin only)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Update Application Status", description = "Update application status (HR/Admin only)")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateApplicationStatusRequest request
    ) {
        log.info("Update application {} status to: {}", id, request.getStatus());

        ApplicationResponse response = applicationService.updateApplicationStatus(id, request);

        return ResponseEntity.ok(ApiResponse.success("Application status updated", response));
    }

    /**
     * Add HR review (HR/Admin only)
     */
    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Add HR Review", description = "Add HR rating and notes to application (HR/Admin only)")
    public ResponseEntity<ApiResponse<ApplicationResponse>> addHrReview(
            @PathVariable Long id,
            @Valid @RequestBody HrReviewRequest request
    ) {
        log.info("Add HR review to application: {}", id);

        ApplicationResponse response = applicationService.addHrReview(id, request);

        return ResponseEntity.ok(ApiResponse.success("Review added successfully", response));
    }

    /**
     * Withdraw application (Candidate only)
     */
    @PatchMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Withdraw Application", description = "Withdraw job application (Candidate only)")
    public ResponseEntity<ApiResponse<ApplicationResponse>> withdrawApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Withdraw application {} by user: {}", id, userDetails.getEmail());

        ApplicationResponse response = applicationService.withdrawApplication(id, userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("Application withdrawn successfully", response));
    }

    /**
     * Get recent applications (HR/Admin only)
     */
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Get Recent Applications", description = "Get recently submitted applications (HR/Admin only)")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getRecentApplications(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("Get recent applications, limit: {}", limit);

        List<ApplicationResponse> response = applicationService.getRecentApplications(limit);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}