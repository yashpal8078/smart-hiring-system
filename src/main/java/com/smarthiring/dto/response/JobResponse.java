package com.smarthiring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smarthiring.enums.JobType;
import com.smarthiring.enums.WorkMode;
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
public class JobResponse {

    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String responsibilities;
    private List<String> requiredSkills;  // Parsed
    private List<String> niceToHaveSkills;  // Parsed
    private Integer experienceMin;
    private Integer experienceMax;
    private String experienceRange;  // e.g., "2-5 years"
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryRange;  // e.g., "5-10 LPA"
    private String salaryCurrency;
    private String location;
    private JobType jobType;
    private String jobTypeDisplay;
    private WorkMode workMode;
    private String workModeDisplay;
    private String department;
    private Integer openings;
    private LocalDate applicationDeadline;
    private Boolean isActive;
    private Boolean isFeatured;
    private Boolean isExpired;
    private Integer viewsCount;
    private Integer applicationsCount;

    // Posted by details
    private Long postedById;
    private String postedByName;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // For candidates - check if already applied
    private Boolean hasApplied;
}