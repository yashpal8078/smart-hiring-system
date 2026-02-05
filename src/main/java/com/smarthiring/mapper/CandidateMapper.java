package com.smarthiring.mapper;

import com.smarthiring.dto.request.CandidateProfileRequest;
import com.smarthiring.dto.response.CandidateResponse;
import com.smarthiring.dto.response.ResumeResponse;
import com.smarthiring.entity.Candidate;
import com.smarthiring.entity.Resume;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CandidateMapper {

    /**
     * Convert Candidate entity to CandidateResponse DTO
     */
    public CandidateResponse toResponse(Candidate candidate) {
        if (candidate == null) {
            return null;
        }

        CandidateResponse response = CandidateResponse.builder()
                .id(candidate.getId())
                .userId(candidate.getUser().getId())
                .email(candidate.getUser().getEmail())
                .fullName(candidate.getUser().getFullName())
                .phone(candidate.getUser().getPhone())
                .profilePicture(candidate.getUser().getProfilePicture())
                .headline(candidate.getHeadline())
                .summary(candidate.getSummary())
                .skills(parseCommaSeparated(candidate.getSkills()))
                .totalExperience(candidate.getTotalExperience())
                .currentCompany(candidate.getCurrentCompany())
                .currentDesignation(candidate.getCurrentDesignation())
                .currentSalary(candidate.getCurrentSalary())
                .expectedSalary(candidate.getExpectedSalary())
                .noticePeriod(candidate.getNoticePeriod())
                .location(candidate.getLocation())
                .preferredLocations(parseCommaSeparated(candidate.getPreferredLocations()))
                .education(candidate.getEducation())
                .linkedinUrl(candidate.getLinkedinUrl())
                .githubUrl(candidate.getGithubUrl())
                .portfolioUrl(candidate.getPortfolioUrl())
                .dateOfBirth(candidate.getDateOfBirth())
                .gender(candidate.getGender())
                .totalResumes(candidate.getResumes() != null ? candidate.getResumes().size() : 0)
                .totalApplications(candidate.getApplications() != null ? candidate.getApplications().size() : 0)
                .createdAt(candidate.getCreatedAt())
                .updatedAt(candidate.getUpdatedAt())
                .build();

        // Set primary resume
        Resume primaryResume = candidate.getPrimaryResume();
        if (primaryResume != null) {
            response.setPrimaryResume(toResumeResponse(primaryResume));
        }

        return response;
    }

    /**
     * Update Candidate entity from request
     */
    public void updateFromRequest(Candidate candidate, CandidateProfileRequest request) {
        if (request.getHeadline() != null) {
            candidate.setHeadline(request.getHeadline());
        }
        if (request.getSummary() != null) {
            candidate.setSummary(request.getSummary());
        }
        if (request.getSkills() != null) {
            candidate.setSkills(request.getSkills());
        }
        if (request.getTotalExperience() != null) {
            candidate.setTotalExperience(request.getTotalExperience());
        }
        if (request.getCurrentCompany() != null) {
            candidate.setCurrentCompany(request.getCurrentCompany());
        }
        if (request.getCurrentDesignation() != null) {
            candidate.setCurrentDesignation(request.getCurrentDesignation());
        }
        if (request.getCurrentSalary() != null) {
            candidate.setCurrentSalary(request.getCurrentSalary());
        }
        if (request.getExpectedSalary() != null) {
            candidate.setExpectedSalary(request.getExpectedSalary());
        }
        if (request.getNoticePeriod() != null) {
            candidate.setNoticePeriod(request.getNoticePeriod());
        }
        if (request.getLocation() != null) {
            candidate.setLocation(request.getLocation());
        }
        if (request.getPreferredLocations() != null) {
            candidate.setPreferredLocations(request.getPreferredLocations());
        }
        if (request.getEducation() != null) {
            candidate.setEducation(request.getEducation());
        }
        if (request.getLinkedinUrl() != null) {
            candidate.setLinkedinUrl(request.getLinkedinUrl());
        }
        if (request.getGithubUrl() != null) {
            candidate.setGithubUrl(request.getGithubUrl());
        }
        if (request.getPortfolioUrl() != null) {
            candidate.setPortfolioUrl(request.getPortfolioUrl());
        }
        if (request.getDateOfBirth() != null) {
            candidate.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            candidate.setGender(request.getGender());
        }

        // Update user fields if provided
        if (request.getFullName() != null) {
            candidate.getUser().setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            candidate.getUser().setPhone(request.getPhone());
        }
    }

    /**
     * Convert Resume to ResumeResponse
     */
    public ResumeResponse toResumeResponse(Resume resume) {
        if (resume == null) {
            return null;
        }

        return ResumeResponse.builder()
                .id(resume.getId())
                .candidateId(resume.getCandidate().getId())
                .fileName(resume.getFileName())
                .originalFileName(resume.getOriginalFileName())
                .fileType(resume.getFileType())
                .fileSize(resume.getFileSize())
                .isPrimary(resume.getIsPrimary())
                .extractedSkills(parseCommaSeparated(resume.getExtractedSkills()))
                .extractedExperience(resume.getExtractedExperience())
                .extractedEducation(resume.getExtractedEducation())
                .uploadedAt(resume.getUploadedAt())
                .downloadUrl("/api/resumes/download/" + resume.getId())
                .build();
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