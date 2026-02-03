package com.smarthiring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smarthiring.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationResponse {

    private Long id;

    // Job details
    private Long jobId;
    private String jobTitle;
    private String jobLocation;
    private String companyName;

    // Candidate details
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String candidateHeadline;

    // Resume details
    private Long resumeId;
    private String resumeFileName;
    private String resumeDownloadUrl;

    // Application details
    private String coverLetter;
    private ApplicationStatus status;
    private String statusDisplay;
    private BigDecimal aiScore;
    private String aiFeedback;
    private Integer hrRating;
    private String hrNotes;

    // Timestamps
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    // Shortlist info
    private Boolean isShortlisted;
    private Long shortlistId;
}