package com.smarthiring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smarthiring.enums.ShortlistStage;
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
public class ShortlistResponse {

    private Long id;

    // Job details
    private Long jobId;
    private String jobTitle;

    // Candidate details
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private BigDecimal candidateExperience;
    private String candidateCurrentCompany;

    // Application details
    private Long applicationId;
    private BigDecimal aiScore;
    private Integer hrRating;

    // Shortlist details
    private ShortlistStage stage;
    private String stageDisplay;
    private String remarks;
    private LocalDateTime interviewDate;

    // Shortlisted by
    private Long shortlistedById;
    private String shortlistedByName;

    private LocalDateTime createdAt;
}