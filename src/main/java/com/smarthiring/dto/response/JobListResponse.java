package com.smarthiring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smarthiring.enums.JobType;
import com.smarthiring.enums.WorkMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobListResponse {

    private Long id;
    private String title;
    private String companyName;  // From postedBy or organization
    private String location;
    private JobType jobType;
    private String jobTypeDisplay;
    private WorkMode workMode;
    private String experienceRange;
    private String salaryRange;
    private List<String> requiredSkills;
    private LocalDate applicationDeadline;
    private Boolean isFeatured;
    private Integer applicationsCount;
    private LocalDateTime createdAt;
    private Boolean hasApplied;

    // Time ago string (e.g., "2 days ago")
    private String postedAgo;
}