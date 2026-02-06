package com.smarthiring.controller;

import com.smarthiring.dto.request.CandidateProfileRequest;
import com.smarthiring.dto.request.CandidateSearchRequest;
import com.smarthiring.dto.response.ApiResponse;
import com.smarthiring.dto.response.CandidateResponse;
import com.smarthiring.dto.response.PagedResponse;
import com.smarthiring.security.CustomUserDetails;
import com.smarthiring.service.CandidateService;
import com.smarthiring.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Candidates", description = "Candidate Profile Management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CandidateController {

    private final CandidateService candidateService;

    /**
     * Get my profile (for logged-in candidate)
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get My Profile", description = "Get profile of currently logged-in candidate")
    public ResponseEntity<ApiResponse<CandidateResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Get profile request from user: {}", userDetails.getEmail());

        CandidateResponse response = candidateService.getCandidateByUserId(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update my profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Update My Profile", description = "Update profile of currently logged-in candidate")
    public ResponseEntity<ApiResponse<CandidateResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CandidateProfileRequest request
    ) {
        log.info("Update profile request from user: {}", userDetails.getEmail());

        CandidateResponse response = candidateService.updateProfile(userDetails.getId(), request);

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    /**
     * Get candidate by ID (HR/Admin only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Get Candidate by ID", description = "Get candidate details by ID (HR/Admin only)")
    public ResponseEntity<ApiResponse<CandidateResponse>> getCandidateById(
            @PathVariable Long id
    ) {
        log.info("Get candidate request for ID: {}", id);

        CandidateResponse response = candidateService.getCandidateById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all candidates (HR/Admin only)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Get All Candidates", description = "Get all candidates with pagination (HR/Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<CandidateResponse>>> getAllCandidates(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        log.info("Get all candidates - page: {}, size: {}", page, size);

        PagedResponse<CandidateResponse> response = candidateService.getAllCandidates(page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Search candidates
     */
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Search Candidates", description = "Search candidates by skills, location, experience (HR/Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<CandidateResponse>>> searchCandidates(
            @RequestBody CandidateSearchRequest request
    ) {
        log.info("Search candidates with criteria: {}", request);

        PagedResponse<CandidateResponse> response = candidateService.searchCandidates(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get candidates by skill
     */
    @GetMapping("/skill/{skill}")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Get Candidates by Skill", description = "Get candidates having specific skill")
    public ResponseEntity<ApiResponse<PagedResponse<CandidateResponse>>> getCandidatesBySkill(
            @PathVariable String skill,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        log.info("Get candidates by skill: {}", skill);

        PagedResponse<CandidateResponse> response = candidateService.getCandidatesBySkill(skill, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all distinct locations
     */
    @GetMapping("/locations")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Get All Locations", description = "Get all distinct candidate locations")
    public ResponseEntity<ApiResponse<List<String>>> getAllLocations() {
        List<String> locations = candidateService.getAllLocations();

        return ResponseEntity.ok(ApiResponse.success(locations));
    }
}