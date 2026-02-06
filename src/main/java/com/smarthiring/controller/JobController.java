package com.smarthiring.controller;

import com.smarthiring.dto.request.JobRequest;
import com.smarthiring.dto.request.JobSearchRequest;
import com.smarthiring.dto.response.ApiResponse;
import com.smarthiring.dto.response.JobListResponse;
import com.smarthiring.dto.response.JobResponse;
import com.smarthiring.dto.response.PagedResponse;
import com.smarthiring.security.CustomUserDetails;
import com.smarthiring.service.CandidateService;
import com.smarthiring.service.JobService;
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
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Jobs", description = "Job Posting and Search APIs")
public class JobController {

    private final JobService jobService;
    private final CandidateService candidateService;

    /**
     * Get all active jobs (Public)
     */
    @GetMapping
    @Operation(summary = "Get All Jobs", description = "Get all active jobs with pagination (Public)")
    public ResponseEntity<ApiResponse<PagedResponse<JobListResponse>>> getAllJobs(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        log.info("Get all jobs - page: {}, size: {}", page, size);

        PagedResponse<JobListResponse> response = jobService.getActiveJobs(page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get job by ID (Public)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Job by ID", description = "Get job details by ID (Public)")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Get job by ID: {}", id);

        Long candidateId = null;
        if (userDetails != null && userDetails.hasRole("ROLE_CANDIDATE")) {
            try {
                candidateId = candidateService.getCandidateByUserId(userDetails.getId()).getId();
            } catch (Exception e) {
                // Candidate profile not found, ignore
            }
        }

        JobResponse response = jobService.getJobById(id, candidateId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Create a new job (HR/Admin only)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create Job", description = "Create a new job posting (HR/Admin only)")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody JobRequest request
    ) {
        log.info("Create job request from user: {}", userDetails.getEmail());

        JobResponse response = jobService.createJob(request, userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job created successfully", response));
    }

    /**
     * Update a job (HR/Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update Job", description = "Update job posting (HR/Admin only)")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody JobRequest request
    ) {
        log.info("Update job {} request from user: {}", id, userDetails.getEmail());

        JobResponse response = jobService.updateJob(id, request, userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", response));
    }

    /**
     * Delete a job (HR/Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete Job", description = "Delete job posting (HR/Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Delete job {} request from user: {}", id, userDetails.getEmail());

        jobService.deleteJob(id, userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully"));
    }

    /**
     * Search jobs (Public)
     */
    @PostMapping("/search")
    @Operation(summary = "Search Jobs", description = "Advanced job search with filters (Public)")
    public ResponseEntity<ApiResponse<PagedResponse<JobListResponse>>> searchJobs(
            @RequestBody JobSearchRequest request
    ) {
        log.info("Search jobs with criteria: {}", request);

        PagedResponse<JobListResponse> response = jobService.searchJobs(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Search jobs by keyword (Public)
     */
    @GetMapping("/search")
    @Operation(summary = "Search Jobs by Keyword", description = "Search jobs by keyword (Public)")
    public ResponseEntity<ApiResponse<PagedResponse<JobListResponse>>> searchJobsByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        log.info("Search jobs by keyword: {}", keyword);

        PagedResponse<JobListResponse> response = jobService.searchJobsByKeyword(keyword, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get featured jobs (Public)
     */
    @GetMapping("/featured")
    @Operation(summary = "Get Featured Jobs", description = "Get all featured jobs (Public)")
    public ResponseEntity<ApiResponse<List<JobListResponse>>> getFeaturedJobs() {
        log.info("Get featured jobs");

        List<JobListResponse> response = jobService.getFeaturedJobs();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get recent jobs (Public)
     */
    @GetMapping("/recent")
    @Operation(summary = "Get Recent Jobs", description = "Get recently posted jobs (Public)")
    public ResponseEntity<ApiResponse<List<JobListResponse>>> getRecentJobs(
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("Get recent jobs, limit: {}", limit);

        List<JobListResponse> response = jobService.getRecentJobs(limit);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get my posted jobs (HR only)
     */
    @GetMapping("/my-jobs")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get My Posted Jobs", description = "Get jobs posted by current HR user")
    public ResponseEntity<ApiResponse<PagedResponse<JobListResponse>>> getMyJobs(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        log.info("Get my jobs for user: {}", userDetails.getEmail());

        PagedResponse<JobListResponse> response = jobService.getJobsByUser(userDetails.getId(), page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Deactivate a job (HR/Admin only)
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Deactivate Job", description = "Deactivate job posting (HR/Admin only)")
    public ResponseEntity<ApiResponse<JobResponse>> deactivateJob(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Deactivate job {} request from user: {}", id, userDetails.getEmail());

        JobResponse response = jobService.deactivateJob(id, userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("Job deactivated successfully", response));
    }

    /**
     * Get all distinct locations (Public)
     */
    @GetMapping("/locations")
    @Operation(summary = "Get All Locations", description = "Get all distinct job locations")
    public ResponseEntity<ApiResponse<List<String>>> getAllLocations() {
        List<String> locations = jobService.getAllLocations();

        return ResponseEntity.ok(ApiResponse.success(locations));
    }

    /**
     * Get all distinct departments (Public)
     */
    @GetMapping("/departments")
    @Operation(summary = "Get All Departments", description = "Get all distinct job departments")
    public ResponseEntity<ApiResponse<List<String>>> getAllDepartments() {
        List<String> departments = jobService.getAllDepartments();

        return ResponseEntity.ok(ApiResponse.success(departments));
    }
}