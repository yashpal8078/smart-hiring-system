package com.smarthiring.dto.request;

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
public class CandidateProfileRequest {

    @Size(max = 200, message = "Headline must not exceed 200 characters")
    private String headline;

    @Size(max = 2000, message = "Summary must not exceed 2000 characters")
    private String summary;

    @NotBlank(message = "Skills are required")
    @Size(max = 1000, message = "Skills must not exceed 1000 characters")
    private String skills;  // Comma-separated

    @DecimalMin(value = "0.0", message = "Experience cannot be negative")
    @DecimalMax(value = "50.0", message = "Experience cannot exceed 50 years")
    private BigDecimal totalExperience;

    @Size(max = 100, message = "Current company must not exceed 100 characters")
    private String currentCompany;

    @Size(max = 100, message = "Current designation must not exceed 100 characters")
    private String currentDesignation;

    @DecimalMin(value = "0", message = "Current salary cannot be negative")
    private BigDecimal currentSalary;

    @DecimalMin(value = "0", message = "Expected salary cannot be negative")
    private BigDecimal expectedSalary;

    @Min(value = 0, message = "Notice period cannot be negative")
    @Max(value = 180, message = "Notice period cannot exceed 180 days")
    private Integer noticePeriod;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @Size(max = 255, message = "Preferred locations must not exceed 255 characters")
    private String preferredLocations;  // Comma-separated

    @Size(max = 255, message = "Education must not exceed 255 characters")
    private String education;

    @Size(max = 255, message = "LinkedIn URL must not exceed 255 characters")
    @Pattern(
            regexp = "^(https?://)?(www\\.)?linkedin\\.com/.*$",
            message = "Please provide a valid LinkedIn URL"
    )
    private String linkedinUrl;

    @Size(max = 255, message = "GitHub URL must not exceed 255 characters")
    @Pattern(
            regexp = "^(https?://)?(www\\.)?github\\.com/.*$",
            message = "Please provide a valid GitHub URL"
    )
    private String githubUrl;

    @Size(max = 255, message = "Portfolio URL must not exceed 255 characters")
    private String portfolioUrl;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be Male, Female, or Other")
    private String gender;

    // User details (can be updated)
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;
}