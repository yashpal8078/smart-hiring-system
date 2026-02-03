package com.smarthiring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CandidateResponse {

    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private String phone;
    private String profilePicture;

    private String headline;
    private String summary;
    private List<String> skills;  // Parsed from comma-separated string
    private BigDecimal totalExperience;
    private String currentCompany;
    private String currentDesignation;
    private BigDecimal currentSalary;
    private BigDecimal expectedSalary;
    private Integer noticePeriod;
    private String location;
    private List<String> preferredLocations;  // Parsed
    private String education;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private LocalDate dateOfBirth;
    private String gender;

    private ResumeResponse primaryResume;
    private Integer totalResumes;
    private Integer totalApplications;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}