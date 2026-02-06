package com.smarthiring.controller;

import com.smarthiring.dto.request.ShortlistRequest;
import com.smarthiring.dto.response.ApiResponse;
import com.smarthiring.dto.response.PagedResponse;
import com.smarthiring.dto.response.ShortlistResponse;
import com.smarthiring.enums.ShortlistStage;
import com.smarthiring.security.CustomUserDetails;
import com.smarthiring.service.ShortlistService;
import com.smarthiring.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/shortlists")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shortlists", description = "Candidate Shortlist Management APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('HR', 'ADMIN')")
public class ShortlistController {

    private final ShortlistService shortlistService;

    /**
     * Shortlist a candidate
     */
    @PostMapping
    @Operation(summary = "Shortlist Candidate", description = "Shortlist a candidate for a job")
    public ResponseEntity<ApiResponse<ShortlistResponse>> shortlistCandidate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ShortlistRequest request
    ) {
        log.info("Shortlist candidate request by HR: {}", userDetails.getEmail());

        ShortlistResponse response = shortlistService.shortlistCandidate(request, userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidate shortlisted successfully", response));
    }

    /**
     * Get shortlist by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Shortlist by ID", description = "Get shortlist details by ID")
    public ResponseEntity<ApiResponse<ShortlistResponse>> getShortlistById(
            @PathVariable Long id
    ) {
        log.info("Get shortlist by ID: {}", id);

        ShortlistResponse response = shortlistService.getShortlistById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get shortlists for a job
     */
    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get Shortlists for Job", description = "Get all shortlisted candidates for a job")
    public ResponseEntity<ApiResponse<PagedResponse<ShortlistResponse>>> getShortlistsByJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        log.info("Get shortlists for job: {}", jobId);

        PagedResponse<ShortlistResponse> response = shortlistService.getShortlistsByJob(jobId, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get shortlists by stage
     */
    @GetMapping("/job/{jobId}/stage/{stage}")
    @Operation(summary = "Get Shortlists by Stage", description = "Get shortlisted candidates by interview stage")
    public ResponseEntity<ApiResponse<List<ShortlistResponse>>> getShortlistsByStage(
            @PathVariable Long jobId,
            @PathVariable ShortlistStage stage
    ) {
        log.info("Get shortlists for job {} at stage {}", jobId, stage);

        List<ShortlistResponse> response = shortlistService.getShortlistsByJobAndStage(jobId, stage);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update shortlist stage
     */
    @PatchMapping("/{id}/stage")
    @Operation(summary = "Update Shortlist Stage", description = "Move candidate to next interview stage")
    public ResponseEntity<ApiResponse<ShortlistResponse>> updateStage(
            @PathVariable Long id,
            @RequestParam ShortlistStage stage
    ) {
        log.info("Update shortlist {} to stage: {}", id, stage);

        ShortlistResponse response = shortlistService.updateStage(id, stage);

        return ResponseEntity.ok(ApiResponse.success("Stage updated successfully", response));
    }

    /**
     * Schedule interview
     */
    @PatchMapping("/{id}/schedule-interview")
    @Operation(summary = "Schedule Interview", description = "Schedule interview for shortlisted candidate")
    public ResponseEntity<ApiResponse<ShortlistResponse>> scheduleInterview(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime interviewDate,
            @RequestParam(required = false) String remarks
    ) {
        log.info("Schedule interview for shortlist {} on {}", id, interviewDate);

        ShortlistResponse response = shortlistService.scheduleInterview(id, interviewDate, remarks);

        return ResponseEntity.ok(ApiResponse.success("Interview scheduled successfully", response));
    }

    /**
     * Remove from shortlist
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove from Shortlist", description = "Remove candidate from shortlist")
    public ResponseEntity<ApiResponse<Void>> removeFromShortlist(
            @PathVariable Long id
    ) {
        log.info("Remove from shortlist: {}", id);

        shortlistService.removeFromShortlist(id);

        return ResponseEntity.ok(ApiResponse.success("Removed from shortlist"));
    }

    /**
     * Get upcoming interviews
     */
    @GetMapping("/upcoming-interviews")
    @Operation(summary = "Get Upcoming Interviews", description = "Get interviews scheduled in next N days")
    public ResponseEntity<ApiResponse<List<ShortlistResponse>>> getUpcomingInterviews(
            @RequestParam(defaultValue = "7") int days
    ) {
        log.info("Get upcoming interviews for next {} days", days);

        List<ShortlistResponse> response = shortlistService.getUpcomingInterviews(days);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get today's interviews
     */
    @GetMapping("/today-interviews")
    @Operation(summary = "Get Today's Interviews", description = "Get interviews scheduled for today")
    public ResponseEntity<ApiResponse<List<ShortlistResponse>>> getTodayInterviews() {
        log.info("Get today's interviews");

        List<ShortlistResponse> response = shortlistService.getTodayInterviews();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}