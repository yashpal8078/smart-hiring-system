package com.smarthiring.service;

import com.smarthiring.dto.request.ApplicationRequest;
import com.smarthiring.dto.request.HrReviewRequest;
import com.smarthiring.dto.request.UpdateApplicationStatusRequest;
import com.smarthiring.dto.response.ApplicationResponse;
import com.smarthiring.dto.response.PagedResponse;
import com.smarthiring.entity.Application;
import com.smarthiring.entity.Candidate;
import com.smarthiring.entity.Job;
import com.smarthiring.entity.Resume;
import com.smarthiring.enums.ApplicationStatus;
import com.smarthiring.exception.BadRequestException;
import com.smarthiring.exception.ResourceNotFoundException;
import com.smarthiring.mapper.ApplicationMapper;
import com.smarthiring.repository.ApplicationRepository;
import com.smarthiring.repository.CandidateRepository;
import com.smarthiring.repository.JobRepository;
import com.smarthiring.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
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
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final ResumeRepository resumeRepository;
    private final ApplicationMapper applicationMapper;
    private final NotificationService notificationService;
    private final AIRankingService aiRankingService;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            JobRepository jobRepository,
            CandidateRepository candidateRepository,
            ResumeRepository resumeRepository,
            ApplicationMapper applicationMapper,
            NotificationService notificationService,
            @Lazy AIRankingService aiRankingService
    ) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.candidateRepository = candidateRepository;
        this.resumeRepository = resumeRepository;
        this.applicationMapper = applicationMapper;
        this.notificationService = notificationService;
        this.aiRankingService = aiRankingService;
    }

    /**
     * Apply for a job
     */
    @Transactional
    public ApplicationResponse applyForJob(ApplicationRequest request, Long userId) {
        // Get candidate
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "userId", userId));

        // Get job
        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", request.getJobId()));

        // Check if job is active
        if (!job.getIsActive()) {
            throw new BadRequestException("This job is no longer accepting applications");
        }

        // Check if job is expired
        if (job.isExpired()) {
            throw new BadRequestException("This job has expired");
        }

        // Check if already applied
        if (applicationRepository.existsByJobIdAndCandidateId(job.getId(), candidate.getId())) {
            throw new BadRequestException("You have already applied for this job");
        }

        // Get resume
        Resume resume = null;
        if (request.getResumeId() != null) {
            resume = resumeRepository.findById(request.getResumeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", request.getResumeId()));

            // Verify resume belongs to candidate
            if (!resume.getCandidate().getId().equals(candidate.getId())) {
                throw new BadRequestException("Resume does not belong to you");
            }
        } else {
            // Get primary resume
            resume = candidate.getPrimaryResume();
        }

        // Create application
        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .resume(resume)
                .coverLetter(request.getCoverLetter())
                .status(ApplicationStatus.APPLIED)
                .build();

        Application savedApplication = applicationRepository.save(application);

        // Increment job application count
        jobRepository.incrementApplicationCount(job.getId());

        // Calculate AI score automatically
        try {
            aiRankingService.calculateScore(savedApplication);
            log.info("AI score calculated for application: {}", savedApplication.getId());
        } catch (Exception e) {
            log.warn("Could not calculate AI score for application {}: {}",
                    savedApplication.getId(), e.getMessage());
        }

        // Send notification to HR
        notificationService.sendApplicationReceivedNotification(job, candidate);

        log.info("Application created: {} for job: {} by candidate: {}",
                savedApplication.getId(), job.getId(), candidate.getId());

        return applicationMapper.toResponse(savedApplication);
    }

    /**
     * Get application by ID
     */
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        return applicationMapper.toResponse(application);
    }

    /**
     * Get applications for a job
     */
    @Transactional(readOnly = true)
    public PagedResponse<ApplicationResponse> getApplicationsByJob(Long jobId, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        Page<Application> applicationsPage = applicationRepository.findByJobId(jobId, pageable);

        List<ApplicationResponse> content = applicationsPage.getContent().stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                applicationsPage.getNumber(),
                applicationsPage.getSize(),
                applicationsPage.getTotalElements(),
                applicationsPage.getTotalPages()
        );
    }

    /**
     * Get applications by candidate (my applications)
     */
    @Transactional(readOnly = true)
    public PagedResponse<ApplicationResponse> getApplicationsByCandidate(Long userId, int page, int size) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "userId", userId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedAt").descending());

        Page<Application> applicationsPage = applicationRepository.findByCandidateId(candidate.getId(), pageable);

        List<ApplicationResponse> content = applicationsPage.getContent().stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                applicationsPage.getNumber(),
                applicationsPage.getSize(),
                applicationsPage.getTotalElements(),
                applicationsPage.getTotalPages()
        );
    }

    /**
     * Update application status
     */
    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, UpdateApplicationStatusRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(request.getStatus());
        application.setHrNotes(request.getNotes());
        application.setUpdatedAt(LocalDateTime.now());

        Application savedApplication = applicationRepository.save(application);

        // Send notification to candidate
        notificationService.sendStatusUpdateNotification(application, oldStatus, request.getStatus());

        log.info("Application {} status updated from {} to {}",
                applicationId, oldStatus, request.getStatus());

        return applicationMapper.toResponse(savedApplication);
    }

    /**
     * Add HR review to application
     */
    @Transactional
    public ApplicationResponse addHrReview(Long applicationId, HrReviewRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        application.setHrRating(request.getRating());
        application.setHrNotes(request.getNotes());
        application.setUpdatedAt(LocalDateTime.now());

        Application savedApplication = applicationRepository.save(application);

        log.info("HR review added to application: {}", applicationId);

        return applicationMapper.toResponse(savedApplication);
    }

    /**
     * Get applications by job sorted by AI score
     */
    @Transactional(readOnly = true)
    public PagedResponse<ApplicationResponse> getApplicationsByAiScore(Long jobId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Application> applicationsPage = applicationRepository.findByJobIdOrderByAiScoreDesc(jobId, pageable);

        List<ApplicationResponse> content = applicationsPage.getContent().stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                applicationsPage.getNumber(),
                applicationsPage.getSize(),
                applicationsPage.getTotalElements(),
                applicationsPage.getTotalPages()
        );
    }

    /**
     * Withdraw application
     */
    @Transactional
    public ApplicationResponse withdrawApplication(Long applicationId, Long userId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "userId", userId));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        // Verify application belongs to candidate
        if (!application.getCandidate().getId().equals(candidate.getId())) {
            throw new BadRequestException("This application does not belong to you");
        }

        // Can only withdraw if status is APPLIED or UNDER_REVIEW
        if (application.getStatus() != ApplicationStatus.APPLIED &&
                application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new BadRequestException("Cannot withdraw application in current status");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setUpdatedAt(LocalDateTime.now());

        Application savedApplication = applicationRepository.save(application);

        log.info("Application {} withdrawn by candidate {}", applicationId, candidate.getId());

        return applicationMapper.toResponse(savedApplication);
    }

    /**
     * Count applications by status
     */
    @Transactional(readOnly = true)
    public long countByStatus(ApplicationStatus status) {
        return applicationRepository.countByStatus(status);
    }

    /**
     * Count applications for a job
     */
    @Transactional(readOnly = true)
    public long countByJobId(Long jobId) {
        return applicationRepository.countByJobId(jobId);
    }

    /**
     * Get recent applications
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getRecentApplications(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return applicationRepository.findRecentApplications(pageable).stream()
                .map(applicationMapper::toSimpleResponse)
                .collect(Collectors.toList());
    }
}