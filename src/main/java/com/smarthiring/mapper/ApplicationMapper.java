package com.smarthiring.mapper;

import com.smarthiring.dto.response.ApplicationResponse;
import com.smarthiring.entity.Application;
import com.smarthiring.entity.Shortlist;
import com.smarthiring.repository.ShortlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationMapper {

    private final ShortlistRepository shortlistRepository;

    /**
     * Convert Application entity to ApplicationResponse DTO
     */
    public ApplicationResponse toResponse(Application application) {
        if (application == null) {
            return null;
        }

        ApplicationResponse response = ApplicationResponse.builder()
                // Job details
                .id(application.getId())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .jobLocation(application.getJob().getLocation())
                .companyName(application.getJob().getPostedBy().getFullName())

                // Candidate details
                .candidateId(application.getCandidate().getId())
                .candidateName(application.getCandidate().getUser().getFullName())
                .candidateEmail(application.getCandidate().getUser().getEmail())
                .candidatePhone(application.getCandidate().getUser().getPhone())
                .candidateHeadline(application.getCandidate().getHeadline())

                // Application details
                .coverLetter(application.getCoverLetter())
                .status(application.getStatus())
                .statusDisplay(application.getStatus() != null ? application.getStatus().getDisplayName() : null)
                .aiScore(application.getAiScore())
                .aiFeedback(application.getAiFeedback())
                .hrRating(application.getHrRating())
                .hrNotes(application.getHrNotes())

                // Timestamps
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .build();

        // Resume details
        if (application.getResume() != null) {
            response.setResumeId(application.getResume().getId());
            response.setResumeFileName(application.getResume().getOriginalFileName());
            response.setResumeDownloadUrl("/api/resumes/download/" + application.getResume().getId());
        }

        // Check if shortlisted
        Shortlist shortlist = shortlistRepository
                .findByJobIdAndCandidateId(application.getJob().getId(), application.getCandidate().getId())
                .orElse(null);

        if (shortlist != null) {
            response.setIsShortlisted(true);
            response.setShortlistId(shortlist.getId());
        } else {
            response.setIsShortlisted(false);
        }

        return response;
    }

    /**
     * Convert Application to simplified response (for listings)
     */
    public ApplicationResponse toSimpleResponse(Application application) {
        if (application == null) {
            return null;
        }

        return ApplicationResponse.builder()
                .id(application.getId())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .candidateId(application.getCandidate().getId())
                .candidateName(application.getCandidate().getUser().getFullName())
                .status(application.getStatus())
                .statusDisplay(application.getStatus() != null ? application.getStatus().getDisplayName() : null)
                .aiScore(application.getAiScore())
                .appliedAt(application.getAppliedAt())
                .build();
    }
}