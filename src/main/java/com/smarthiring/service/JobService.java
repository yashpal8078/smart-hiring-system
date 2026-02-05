package com.smarthiring.service;

import com.smarthiring.dto.request.JobRequest;
import com.smarthiring.dto.request.JobSearchRequest;
import com.smarthiring.dto.response.JobListResponse;
import com.smarthiring.dto.response.JobResponse;
import com.smarthiring.dto.response.PagedResponse;
import com.smarthiring.entity.Job;
import com.smarthiring.entity.User;
import com.smarthiring.exception.BadRequestException;
import com.smarthiring.exception.ResourceNotFoundException;
import com.smarthiring.mapper.JobMapper;
import com.smarthiring.repository.ApplicationRepository;
import com.smarthiring.repository.JobRepository;
import com.smarthiring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final JobMapper jobMapper;

    /**
     * Create a new job
     */
    @Transactional
    public JobResponse createJob(JobRequest request, Long postedById) {
        User postedBy = userRepository.findById(postedById)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", postedById));

        Job job = jobMapper.toEntity(request);
        job.setPostedBy(postedBy);

        Job savedJob = jobRepository.save(job);

        log.info("Job created: {} by user: {}", savedJob.getId(), postedById);

        return jobMapper.toResponse(savedJob);
    }

    /**
     * Update a job
     */
    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request, Long userId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        // Check if user is the owner or admin
        if (!job.getPostedBy().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to update this job");
        }

        jobMapper.updateFromRequest(job, request);
        Job savedJob = jobRepository.save(job);

        log.info("Job updated: {}", jobId);

        return jobMapper.toResponse(savedJob);
    }

    /**
     * Get job by ID
     */
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        // Increment view count
        jobRepository.incrementViewCount(jobId);

        return jobMapper.toResponse(job);
    }

    /**
     * Get job by ID with candidate context (check if applied)
     */
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId, Long candidateId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        // Increment view count
        jobRepository.incrementViewCount(jobId);

        JobResponse response = jobMapper.toResponse(job);

        // Check if candidate has applied
        if (candidateId != null) {
            boolean hasApplied = applicationRepository.existsByJobIdAndCandidateId(jobId, candidateId);
            response.setHasApplied(hasApplied);
        }

        return response;
    }

    /**
     * Get all active jobs
     */
    @Transactional(readOnly = true)
    public PagedResponse<JobListResponse> getActiveJobs(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Job> jobsPage = jobRepository.findByIsActiveTrue(pageable);

        List<JobListResponse> content = jobsPage.getContent().stream()
                .map(jobMapper::toListResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                jobsPage.getNumber(),
                jobsPage.getSize(),
                jobsPage.getTotalElements(),
                jobsPage.getTotalPages()
        );
    }

    /**
     * Search jobs
     */
    @Transactional(readOnly = true)
    public PagedResponse<JobListResponse> searchJobs(JobSearchRequest request) {
        Sort sort = request.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(request.getSortBy()).descending()
                : Sort.by(request.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Job> jobsPage = jobRepository.advancedSearch(
                request.getKeyword(),
                request.getLocation(),
                request.getJobType(),
                request.getWorkMode(),
                request.getMinExperience(),
                request.getMaxExperience(),
                pageable
        );

        List<JobListResponse> content = jobsPage.getContent().stream()
                .map(jobMapper::toListResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                jobsPage.getNumber(),
                jobsPage.getSize(),
                jobsPage.getTotalElements(),
                jobsPage.getTotalPages()
        );
    }

    /**
     * Get jobs by keyword search
     */
    @Transactional(readOnly = true)
    public PagedResponse<JobListResponse> searchJobsByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Job> jobsPage = jobRepository.searchJobs(keyword, pageable);

        List<JobListResponse> content = jobsPage.getContent().stream()
                .map(jobMapper::toListResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                jobsPage.getNumber(),
                jobsPage.getSize(),
                jobsPage.getTotalElements(),
                jobsPage.getTotalPages()
        );
    }

    /**
     * Get jobs posted by a user
     */
    @Transactional(readOnly = true)
    public PagedResponse<JobListResponse> getJobsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Job> jobsPage = jobRepository.findByPostedById(userId, pageable);

        List<JobListResponse> content = jobsPage.getContent().stream()
                .map(jobMapper::toListResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                jobsPage.getNumber(),
                jobsPage.getSize(),
                jobsPage.getTotalElements(),
                jobsPage.getTotalPages()
        );
    }

    /**
     * Get featured jobs
     */
    @Transactional(readOnly = true)
    public List<JobListResponse> getFeaturedJobs() {
        return jobRepository.findByIsFeaturedTrueAndIsActiveTrue().stream()
                .map(jobMapper::toListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get recent jobs
     */
    @Transactional(readOnly = true)
    public List<JobListResponse> getRecentJobs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return jobRepository.findRecentJobs(pageable).stream()
                .map(jobMapper::toListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deactivate a job
     */
    @Transactional
    public JobResponse deactivateJob(Long jobId, Long userId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (!job.getPostedBy().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to deactivate this job");
        }

        job.setIsActive(false);
        Job savedJob = jobRepository.save(job);

        log.info("Job deactivated: {}", jobId);

        return jobMapper.toResponse(savedJob);
    }

    /**
     * Delete a job
     */
    @Transactional
    public void deleteJob(Long jobId, Long userId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (!job.getPostedBy().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to delete this job");
        }

        jobRepository.delete(job);
        log.info("Job deleted: {}", jobId);
    }

    /**
     * Get all distinct locations
     */
    @Transactional(readOnly = true)
    public List<String> getAllLocations() {
        return jobRepository.findAllDistinctLocations();
    }

    /**
     * Get all distinct departments
     */
    @Transactional(readOnly = true)
    public List<String> getAllDepartments() {
        return jobRepository.findAllDistinctDepartments();
    }

    /**
     * Count active jobs
     */
    @Transactional(readOnly = true)
    public long countActiveJobs() {
        return jobRepository.countByIsActiveTrue();
    }

    /**
     * Deactivate expired jobs (scheduled task)
     */
    @Transactional
    public int deactivateExpiredJobs() {
        int count = jobRepository.deactivateExpiredJobs(LocalDate.now());
        if (count > 0) {
            log.info("Deactivated {} expired jobs", count);
        }
        return count;
    }
}