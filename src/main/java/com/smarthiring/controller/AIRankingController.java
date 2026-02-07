package com.smarthiring.controller;

import com.smarthiring.dto.response.ApiResponse;
import com.smarthiring.dto.response.ApplicationResponse;
import com.smarthiring.service.AIRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Ranking", description = "AI-Powered Resume Ranking and Scoring APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('HR', 'ADMIN')")
public class AIRankingController {

    private final AIRankingService aiRankingService;

    /**
     * Score a single application
     */
    @PostMapping("/score/{applicationId}")
    @Operation(summary = "Score Application", description = "Calculate AI score for a single application")
    public ResponseEntity<ApiResponse<Map<String, Object>>> scoreApplication(
            @PathVariable Long applicationId
    ) {
        log.info("Scoring application: {}", applicationId);

        BigDecimal score = aiRankingService.calculateApplicationScore(applicationId);

        Map<String, Object> result = Map.of(
                "applicationId", applicationId,
                "aiScore", score,
                "message", "Application scored successfully"
        );

        return ResponseEntity.ok(ApiResponse.success("Application scored", result));
    }

    /**
     * Score all applications for a job
     */
    @PostMapping("/score/job/{jobId}")
    @Operation(summary = "Score All Applications for Job", description = "Calculate AI scores for all applications of a job")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> scoreAllForJob(
            @PathVariable Long jobId
    ) {
        log.info("Scoring all applications for job: {}", jobId);

        List<ApplicationResponse> scoredApplications = aiRankingService.scoreAllApplicationsForJob(jobId);

        return ResponseEntity.ok(ApiResponse.success(
                "Scored " + scoredApplications.size() + " applications",
                scoredApplications
        ));
    }

    /**
     * Get top candidates for a job
     */
    @GetMapping("/top-candidates/{jobId}")
    @Operation(summary = "Get Top Candidates", description = "Get top N candidates for a job based on AI score")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getTopCandidates(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("Getting top {} candidates for job: {}", limit, jobId);

        List<ApplicationResponse> topCandidates = aiRankingService.getTopCandidates(jobId, limit);

        return ResponseEntity.ok(ApiResponse.success(topCandidates));
    }

    /**
     * Get candidates above threshold
     */
    @GetMapping("/qualified-candidates/{jobId}")
    @Operation(summary = "Get Qualified Candidates", description = "Get candidates with AI score above threshold")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getQualifiedCandidates(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "70.0") double threshold
    ) {
        log.info("Getting candidates above {}% for job: {}", threshold, jobId);

        List<ApplicationResponse> qualifiedCandidates = aiRankingService
                .getCandidatesAboveThreshold(jobId, threshold);

        return ResponseEntity.ok(ApiResponse.success(
                "Found " + qualifiedCandidates.size() + " qualified candidates",
                qualifiedCandidates
        ));
    }

    /**
     * Get score statistics for a job
     */
    @GetMapping("/statistics/{jobId}")
    @Operation(summary = "Get Score Statistics", description = "Get AI score statistics for a job")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getScoreStatistics(
            @PathVariable Long jobId
    ) {
        log.info("Getting score statistics for job: {}", jobId);

        Map<String, Object> stats = aiRankingService.getScoreStatistics(jobId);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get match explanation for an application
     */
    @GetMapping("/explanation/{applicationId}")
    @Operation(summary = "Get Match Explanation", description = "Get detailed explanation of AI score for an application")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMatchExplanation(
            @PathVariable Long applicationId
    ) {
        log.info("Getting match explanation for application: {}", applicationId);

        Map<String, Object> explanation = aiRankingService.getMatchExplanation(applicationId);

        return ResponseEntity.ok(ApiResponse.success(explanation));
    }

    /**
     * Re-score applications that need scoring
     */
    @PostMapping("/rescore/{jobId}")
    @Operation(summary = "Re-score Applications", description = "Re-score all applications that need scoring for a job")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rescoreApplications(
            @PathVariable Long jobId
    ) {
        log.info("Re-scoring applications for job: {}", jobId);

        int count = aiRankingService.rescoreApplications(jobId);

        Map<String, Object> result = Map.of(
                "jobId", jobId,
                "applicationsRescored", count
        );

        return ResponseEntity.ok(ApiResponse.success("Re-scored " + count + " applications", result));
    }
}