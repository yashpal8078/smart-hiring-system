package com.smarthiring.dto.request;

import com.smarthiring.enums.JobType;
import com.smarthiring.enums.WorkMode;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequest {

    @NotBlank(message = "Job title is required")
    @Size(min = 3, max = 200, message = "Job title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Job description is required")
    @Size(min = 50, max = 5000, message = "Job description must be between 50 and 5000 characters")
    private String description;

    @Size(max = 2000, message = "Requirements must not exceed 2000 characters")
    private String requirements;

    @Size(max = 2000, message = "Responsibilities must not exceed 2000 characters")
    private String responsibilities;

    @NotBlank(message = "Required skills are required")
    @Size(max = 1000, message = "Required skills must not exceed 1000 characters")
    private String requiredSkills;  // Comma-separated

    @Size(max = 1000, message = "Nice to have skills must not exceed 1000 characters")
    private String niceToHaveSkills;  // Comma-separated

    @Min(value = 0, message = "Minimum experience cannot be negative")
    private Integer experienceMin;

    @Min(value = 0, message = "Maximum experience cannot be negative")
    private Integer experienceMax;

    @DecimalMin(value = "0", message = "Minimum salary cannot be negative")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0", message = "Maximum salary cannot be negative")
    private BigDecimal salaryMax;

    @Size(max = 10, message = "Currency code must not exceed 10 characters")
    private String salaryCurrency;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    private WorkMode workMode;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;

    @Min(value = 1, message = "Number of openings must be at least 1")
    private Integer openings;

    @Future(message = "Application deadline must be in the future")
    private LocalDate applicationDeadline;

    private Boolean isFeatured;
}