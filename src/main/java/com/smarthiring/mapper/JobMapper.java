package com.smarthiring.mapper;

import com.smarthiring.dto.request.JobRequest;
import com.smarthiring.dto.response.JobListResponse;
import com.smarthiring.dto.response.JobResponse;
import com.smarthiring.entity.Job;
import com.smarthiring.util.DateUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JobMapper {

    /**
     * Convert Job entity to JobResponse DTO
     */
    public JobResponse toResponse(Job job) {
        if (job == null) {
            return null;
        }

        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .responsibilities(job.getResponsibilities())
                .requiredSkills(parseCommaSeparated(job.getRequiredSkills()))
                .niceToHaveSkills(parseCommaSeparated(job.getNiceToHaveSkills()))
                .experienceMin(job.getExperienceMin())
                .experienceMax(job.getExperienceMax())
                .experienceRange(formatExperienceRange(job.getExperienceMin(), job.getExperienceMax()))
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryRange(formatSalaryRange(job.getSalaryMin(), job.getSalaryMax(), job.getSalaryCurrency()))
                .salaryCurrency(job.getSalaryCurrency())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .jobTypeDisplay(job.getJobType() != null ? job.getJobType().getDisplayName() : null)
                .workMode(job.getWorkMode())
                .workModeDisplay(job.getWorkMode() != null ? job.getWorkMode().getDisplayName() : null)
                .department(job.getDepartment())
                .openings(job.getOpenings())
                .applicationDeadline(job.getApplicationDeadline())
                .isActive(job.getIsActive())
                .isFeatured(job.getIsFeatured())
                .isExpired(job.isExpired())
                .viewsCount(job.getViewsCount())
                .applicationsCount(job.getApplicationsCount())
                .postedById(job.getPostedBy().getId())
                .postedByName(job.getPostedBy().getFullName())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    /**
     * Convert Job entity to JobListResponse (for listing)
     */
    public JobListResponse toListResponse(Job job) {
        if (job == null) {
            return null;
        }

        return JobListResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(job.getPostedBy().getFullName()) // or organization name
                .location(job.getLocation())
                .jobType(job.getJobType())
                .jobTypeDisplay(job.getJobType() != null ? job.getJobType().getDisplayName() : null)
                .workMode(job.getWorkMode())
                .experienceRange(formatExperienceRange(job.getExperienceMin(), job.getExperienceMax()))
                .salaryRange(formatSalaryRange(job.getSalaryMin(), job.getSalaryMax(), job.getSalaryCurrency()))
                .requiredSkills(parseCommaSeparated(job.getRequiredSkills()))
                .applicationDeadline(job.getApplicationDeadline())
                .isFeatured(job.getIsFeatured())
                .applicationsCount(job.getApplicationsCount())
                .createdAt(job.getCreatedAt())
                .postedAgo(DateUtils.getTimeAgo(job.getCreatedAt()))
                .build();
    }

    /**
     * Create Job entity from JobRequest
     */
    public Job toEntity(JobRequest request) {
        if (request == null) {
            return null;
        }

        return Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .requirements(request.getRequirements())
                .responsibilities(request.getResponsibilities())
                .requiredSkills(request.getRequiredSkills())
                .niceToHaveSkills(request.getNiceToHaveSkills())
                .experienceMin(request.getExperienceMin() != null ? request.getExperienceMin() : 0)
                .experienceMax(request.getExperienceMax())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .salaryCurrency(request.getSalaryCurrency() != null ? request.getSalaryCurrency() : "INR")
                .location(request.getLocation())
                .jobType(request.getJobType())
                .workMode(request.getWorkMode())
                .department(request.getDepartment())
                .openings(request.getOpenings() != null ? request.getOpenings() : 1)
                .applicationDeadline(request.getApplicationDeadline())
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .isActive(true)
                .viewsCount(0)
                .applicationsCount(0)
                .build();
    }

    /**
     * Update Job entity from JobRequest
     */
    public void updateFromRequest(Job job, JobRequest request) {
        if (request.getTitle() != null) {
            job.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            job.setDescription(request.getDescription());
        }
        if (request.getRequirements() != null) {
            job.setRequirements(request.getRequirements());
        }
        if (request.getResponsibilities() != null) {
            job.setResponsibilities(request.getResponsibilities());
        }
        if (request.getRequiredSkills() != null) {
            job.setRequiredSkills(request.getRequiredSkills());
        }
        if (request.getNiceToHaveSkills() != null) {
            job.setNiceToHaveSkills(request.getNiceToHaveSkills());
        }
        if (request.getExperienceMin() != null) {
            job.setExperienceMin(request.getExperienceMin());
        }
        if (request.getExperienceMax() != null) {
            job.setExperienceMax(request.getExperienceMax());
        }
        if (request.getSalaryMin() != null) {
            job.setSalaryMin(request.getSalaryMin());
        }
        if (request.getSalaryMax() != null) {
            job.setSalaryMax(request.getSalaryMax());
        }
        if (request.getSalaryCurrency() != null) {
            job.setSalaryCurrency(request.getSalaryCurrency());
        }
        if (request.getLocation() != null) {
            job.setLocation(request.getLocation());
        }
        if (request.getJobType() != null) {
            job.setJobType(request.getJobType());
        }
        if (request.getWorkMode() != null) {
            job.setWorkMode(request.getWorkMode());
        }
        if (request.getDepartment() != null) {
            job.setDepartment(request.getDepartment());
        }
        if (request.getOpenings() != null) {
            job.setOpenings(request.getOpenings());
        }
        if (request.getApplicationDeadline() != null) {
            job.setApplicationDeadline(request.getApplicationDeadline());
        }
        if (request.getIsFeatured() != null) {
            job.setIsFeatured(request.getIsFeatured());
        }
    }

    /**
     * Format experience range string
     */
    private String formatExperienceRange(Integer min, Integer max) {
        if (min == null && max == null) {
            return "Not specified";
        }
        if (min == null) {
            return "Up to " + max + " years";
        }
        if (max == null) {
            return min + "+ years";
        }
        if (min.equals(max)) {
            return min + " years";
        }
        return min + "-" + max + " years";
    }

    /**
     * Format salary range string
     */
    private String formatSalaryRange(BigDecimal min, BigDecimal max, String currency) {
        if (min == null && max == null) {
            return "Not disclosed";
        }

        String curr = currency != null ? currency : "INR";

        if (min == null) {
            return "Up to " + formatSalary(max) + " " + curr;
        }
        if (max == null) {
            return formatSalary(min) + "+ " + curr;
        }
        return formatSalary(min) + " - " + formatSalary(max) + " " + curr;
    }

    /**
     * Format salary (e.g., 1000000 -> 10 LPA)
     */
    private String formatSalary(BigDecimal salary) {
        if (salary == null) {
            return "0";
        }
        double value = salary.doubleValue();
        if (value >= 10000000) {
            return String.format("%.1f Cr", value / 10000000);
        } else if (value >= 100000) {
            return String.format("%.1f LPA", value / 100000);
        } else if (value >= 1000) {
            return String.format("%.0fK", value / 1000);
        }
        return String.format("%.0f", value);
    }

    /**
     * Parse comma-separated string to list
     */
    private List<String> parseCommaSeparated(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}