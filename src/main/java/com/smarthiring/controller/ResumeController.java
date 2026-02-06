package com.smarthiring.controller;

import com.smarthiring.dto.response.ApiResponse;
import com.smarthiring.dto.response.ResumeResponse;
import com.smarthiring.security.CustomUserDetails;
import com.smarthiring.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Resumes", description = "Resume Upload and Management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * Upload resume
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Upload Resume", description = "Upload a resume file (PDF, DOC, DOCX)")
    public ResponseEntity<ApiResponse<ResumeResponse>> uploadResume(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "makePrimary", defaultValue = "false") boolean makePrimary
    ) {
        log.info("Upload resume request from user: {}", userDetails.getEmail());

        ResumeResponse response = resumeService.uploadResume(userDetails.getId(), file, makePrimary);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resume uploaded successfully", response));
    }

    /**
     * Get my resumes
     */
    @GetMapping("/my-resumes")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get My Resumes", description = "Get all resumes uploaded by current user")
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> getMyResumes(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Get my resumes for user: {}", userDetails.getEmail());

        List<ResumeResponse> response = resumeService.getResumesByCandidate(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get primary resume
     */
    @GetMapping("/primary")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get Primary Resume", description = "Get the primary resume of current user")
    public ResponseEntity<ApiResponse<ResumeResponse>> getPrimaryResume(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Get primary resume for user: {}", userDetails.getEmail());

        ResumeResponse response = resumeService.getPrimaryResume(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get resume by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Resume by ID", description = "Get resume details by ID")
    public ResponseEntity<ApiResponse<ResumeResponse>> getResumeById(
            @PathVariable Long id
    ) {
        log.info("Get resume by ID: {}", id);

        ResumeResponse response = resumeService.getResumeById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Download resume
     */
    @GetMapping("/download/{id}")
    @Operation(summary = "Download Resume", description = "Download resume file by ID")
    public ResponseEntity<Resource> downloadResume(
            @PathVariable Long id
    ) {
        log.info("Download resume: {}", id);

        Resource resource = resumeService.downloadResume(id);
        String contentType = resumeService.getResumeContentType(id);
        String filename = resumeService.getResumeOriginalFilename(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    /**
     * Set resume as primary
     */
    @PatchMapping("/{id}/primary")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Set Primary Resume", description = "Set a resume as the primary resume")
    public ResponseEntity<ApiResponse<ResumeResponse>> setPrimaryResume(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        log.info("Set primary resume {} for user: {}", id, userDetails.getEmail());

        ResumeResponse response = resumeService.setPrimaryResume(userDetails.getId(), id);

        return ResponseEntity.ok(ApiResponse.success("Resume set as primary", response));
    }

    /**
     * Delete resume
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Delete Resume", description = "Delete a resume")
    public ResponseEntity<ApiResponse<Void>> deleteResume(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        log.info("Delete resume {} for user: {}", id, userDetails.getEmail());

        resumeService.deleteResume(userDetails.getId(), id);

        return ResponseEntity.ok(ApiResponse.success("Resume deleted successfully"));
    }

    /**
     * Re-parse resume (extract skills again)
     */
    @PostMapping("/{id}/reparse")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @Operation(summary = "Re-parse Resume", description = "Re-parse resume to extract skills (HR/Admin only)")
    public ResponseEntity<ApiResponse<ResumeResponse>> reparseResume(
            @PathVariable Long id
    ) {
        log.info("Re-parse resume: {}", id);

        ResumeResponse response = resumeService.reparseResume(id);

        return ResponseEntity.ok(ApiResponse.success("Resume re-parsed successfully", response));
    }
}