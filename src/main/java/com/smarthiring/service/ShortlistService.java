package com.smarthiring.service;

import com.smarthiring.dto.request.ShortlistRequest;
import com.smarthiring.dto.response.PagedResponse;
import com.smarthiring.dto.response.ShortlistResponse;
import com.smarthiring.entity.*;
import com.smarthiring.enums.ApplicationStatus;
import com.smarthiring.enums.ShortlistStage;
import com.smarthiring.exception.BadRequestException;
import com.smarthiring.exception.ResourceNotFoundException;
import com.smarthiring.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortlistService {

    private final ShortlistRepository shortlistRepository;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Shortlist a candidate
     */
    @Transactional
    public ShortlistResponse shortlistCandidate(ShortlistRequest request, Long hrUserId) {
        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", request.getApplicationId()));

        // Check if already shortlisted
        if (shortlistRepository.existsByJobIdAndCandidateId(
                application.getJob().getId(), application.getCandidate().getId())) {
            throw new BadRequestException("Candidate is already shortlisted for this job");
        }

        User hr = userRepository.findById(hrUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", hrUserId));

        // Create shortlist
        Shortlist shortlist = Shortlist.builder()
                .job(application.getJob())
                .candidate(application.getCandidate())
                .application(application)
                .shortlistedBy(hr)
                .stage(request.getStage() != null ? request.getStage() : ShortlistStage.INITIAL)
                .remarks(request.getRemarks())
                .interviewDate(request.getInterviewDate())
                .build();

        Shortlist savedShortlist = shortlistRepository.save(shortlist);

        // Update application status
        application.setStatus(ApplicationStatus.SHORTLISTED);
        application.setUpdatedAt(LocalDateTime.now());
        applicationRepository.save(application);

        // Send notification
        notificationService.sendShortlistNotification(savedShortlist);

        log.info("Candidate {} shortlisted for job {} by HR {}",
                application.getCandidate().getId(), application.getJob().getId(), hrUserId);

        return toResponse(savedShortlist);
    }

    /**
     * Get shortlist by ID
     */
    @Transactional(readOnly = true)
    public ShortlistResponse getShortlistById(Long shortlistId) {
        Shortlist shortlist = shortlistRepository.findById(shortlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Shortlist", "id", shortlistId));

        return toResponse(shortlist);
    }

    /**
     * Get shortlists for a job
     */
    @Transactional(readOnly = true)
    public PagedResponse<ShortlistResponse> getShortlistsByJob(Long jobId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Shortlist> shortlistPage = shortlistRepository.findByJobId(jobId, pageable);

        List<ShortlistResponse> content = shortlistPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                shortlistPage.getNumber(),
                shortlistPage.getSize(),
                shortlistPage.getTotalElements(),
                shortlistPage.getTotalPages()
        );
    }

    /**
     * Get shortlists by stage
     */
    @Transactional(readOnly = true)
    public List<ShortlistResponse> getShortlistsByJobAndStage(Long jobId, ShortlistStage stage) {
        return shortlistRepository.findByJobIdAndStage(jobId, stage).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update shortlist stage
     */
    @Transactional
    public ShortlistResponse updateStage(Long shortlistId, ShortlistStage newStage) {
        Shortlist shortlist = shortlistRepository.findById(shortlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Shortlist", "id", shortlistId));

        shortlist.setStage(newStage);
        Shortlist savedShortlist = shortlistRepository.save(shortlist);

        // Update application status based on stage
        if (newStage == ShortlistStage.FINAL) {
            shortlist.getApplication().setStatus(ApplicationStatus.INTERVIEWED);
            applicationRepository.save(shortlist.getApplication());
        }

        log.info("Shortlist {} stage updated to {}", shortlistId, newStage);

        return toResponse(savedShortlist);
    }

    /**
     * Schedule interview
     */
    @Transactional
    public ShortlistResponse scheduleInterview(Long shortlistId, LocalDateTime interviewDate, String remarks) {
        Shortlist shortlist = shortlistRepository.findById(shortlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Shortlist", "id", shortlistId));

        shortlist.setInterviewDate(interviewDate);
        if (remarks != null) {
            shortlist.setRemarks(remarks);
        }

        Shortlist savedShortlist = shortlistRepository.save(shortlist);

        // Update application status
        shortlist.getApplication().setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(shortlist.getApplication());

        // Send notification
        notificationService.sendInterviewNotification(savedShortlist);

        log.info("Interview scheduled for shortlist {} on {}", shortlistId, interviewDate);

        return toResponse(savedShortlist);
    }

    /**
     * Remove from shortlist
     */
    @Transactional
    public void removeFromShortlist(Long shortlistId) {
        Shortlist shortlist = shortlistRepository.findById(shortlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Shortlist", "id", shortlistId));

        // Update application status back to under review
        shortlist.getApplication().setStatus(ApplicationStatus.UNDER_REVIEW);
        applicationRepository.save(shortlist.getApplication());

        shortlistRepository.delete(shortlist);

        log.info("Shortlist {} removed", shortlistId);
    }

    /**
     * Get upcoming interviews
     */
    @Transactional(readOnly = true)
    public List<ShortlistResponse> getUpcomingInterviews(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(days);

        return shortlistRepository.findUpcomingInterviews(now, endDate).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get today's interviews
     */
    @Transactional(readOnly = true)
    public List<ShortlistResponse> getTodayInterviews() {
        return shortlistRepository.findTodayInterviews(LocalDateTime.now()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Count shortlists by job
     */
    @Transactional(readOnly = true)
    public long countByJobId(Long jobId) {
        return shortlistRepository.countByJobId(jobId);
    }

    /**
     * Convert entity to response
     */
    private ShortlistResponse toResponse(Shortlist shortlist) {
        return ShortlistResponse.builder()
                .id(shortlist.getId())
                .jobId(shortlist.getJob().getId())
                .jobTitle(shortlist.getJob().getTitle())
                .candidateId(shortlist.getCandidate().getId())
                .candidateName(shortlist.getCandidate().getUser().getFullName())
                .candidateEmail(shortlist.getCandidate().getUser().getEmail())
                .candidatePhone(shortlist.getCandidate().getUser().getPhone())
                .candidateExperience(shortlist.getCandidate().getTotalExperience())
                .candidateCurrentCompany(shortlist.getCandidate().getCurrentCompany())
                .applicationId(shortlist.getApplication().getId())
                .aiScore(shortlist.getApplication().getAiScore())
                .hrRating(shortlist.getApplication().getHrRating())
                .stage(shortlist.getStage())
                .stageDisplay(shortlist.getStage().getDisplayName())
                .remarks(shortlist.getRemarks())
                .interviewDate(shortlist.getInterviewDate())
                .shortlistedById(shortlist.getShortlistedBy().getId())
                .shortlistedByName(shortlist.getShortlistedBy().getFullName())
                .createdAt(shortlist.getCreatedAt())
                .build();
    }
}