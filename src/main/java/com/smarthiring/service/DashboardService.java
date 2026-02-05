package com.smarthiring.service;

import com.smarthiring.dto.response.ApplicationResponse;
import com.smarthiring.dto.response.DashboardStatsResponse;
import com.smarthiring.dto.response.JobListResponse;
import com.smarthiring.enums.ApplicationStatus;
import com.smarthiring.enums.RoleName;
import com.smarthiring.mapper.ApplicationMapper;
import com.smarthiring.mapper.JobMapper;
import com.smarthiring.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final ShortlistRepository shortlistRepository;
    private final JobMapper jobMapper;
    private final ApplicationMapper applicationMapper;

    /**
     * Get admin/HR dashboard stats
     */
    @Transactional(readOnly = true)
    public DashboardStatsResponse getAdminDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        // Basic counts
        stats.setTotalJobs(jobRepository.count());
        stats.setActiveJobs(jobRepository.countByIsActiveTrue());
        stats.setTotalCandidates(candidateRepository.count());
        stats.setTotalApplications(applicationRepository.count());

        // Application status counts
        stats.setPendingApplications(
                applicationRepository.countByStatus(ApplicationStatus.APPLIED) +
                        applicationRepository.countByStatus(ApplicationStatus.UNDER_REVIEW)
        );
        stats.setShortlistedCandidates(
                applicationRepository.countByStatus(ApplicationStatus.SHORTLISTED)
        );
        stats.setHiredCandidates(
                applicationRepository.countByStatus(ApplicationStatus.HIRED)
        );

        // Applications by status chart data
        List<Map<String, Object>> statusData = new ArrayList<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            Map<String, Object> item = new HashMap<>();
            item.put("status", status.getDisplayName());
            item.put("count", applicationRepository.countByStatus(status));
            statusData.add(item);
        }
        stats.setApplicationsByStatus(statusData);

        // Recent applications
        List<ApplicationResponse> recentApps = applicationRepository
                .findRecentApplications(PageRequest.of(0, 5))
                .stream()
                .map(applicationMapper::toSimpleResponse)
                .collect(Collectors.toList());
        stats.setRecentApplications(recentApps);

        // Recent jobs
        List<JobListResponse> recentJobs = jobRepository
                .findRecentJobs(PageRequest.of(0, 5))
                .stream()
                .map(jobMapper::toListResponse)
                .collect(Collectors.toList());
        stats.setRecentJobs(recentJobs);

        return stats;
    }

    /**
     * Get candidate dashboard stats
     */
    @Transactional(readOnly = true)
    public DashboardStatsResponse getCandidateDashboardStats(Long candidateId) {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        // Application counts
        List<com.smarthiring.entity.Application> applications =
                applicationRepository.findByCandidateId(candidateId);

        stats.setAppliedJobs((long) applications.size());

        stats.setShortlistedJobs(applications.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED ||
                        a.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED)
                .count());

        stats.setInterviewsScheduled(applications.stream()
                .filter(a -> a.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED)
                .count());

        // Applications by status
        List<Map<String, Object>> statusData = new ArrayList<>();
        Map<ApplicationStatus, Long> statusCounts = applications.stream()
                .collect(Collectors.groupingBy(
                        com.smarthiring.entity.Application::getStatus,
                        Collectors.counting()
                ));

        for (Map.Entry<ApplicationStatus, Long> entry : statusCounts.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("status", entry.getKey().getDisplayName());
            item.put("count", entry.getValue());
            statusData.add(item);
        }
        stats.setApplicationsByStatus(statusData);

        // Recent jobs (recommendations)
        List<JobListResponse> recentJobs = jobRepository
                .findRecentJobs(PageRequest.of(0, 5))
                .stream()
                .map(jobMapper::toListResponse)
                .collect(Collectors.toList());
        stats.setRecentJobs(recentJobs);

        return stats;
    }

    /**
     * Get HR dashboard stats (for specific HR user)
     */
    @Transactional(readOnly = true)
    public DashboardStatsResponse getHrDashboardStats(Long hrUserId) {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        // Jobs posted by this HR
        long postedJobs = jobRepository.countByPostedById(hrUserId);
        stats.setTotalJobs(postedJobs);

        // Get all job IDs posted by this HR
        List<Long> jobIds = jobRepository.findByPostedById(hrUserId, PageRequest.of(0, 1000))
                .getContent()
                .stream()
                .map(j -> j.getId())
                .collect(Collectors.toList());

        // Count applications for these jobs
        long totalApps = 0;
        long pendingApps = 0;
        long shortlisted = 0;

        for (Long jobId : jobIds) {
            totalApps += applicationRepository.countByJobId(jobId);
            pendingApps += applicationRepository.countByJobIdAndStatus(jobId, ApplicationStatus.APPLIED);
            shortlisted += applicationRepository.countByJobIdAndStatus(jobId, ApplicationStatus.SHORTLISTED);
        }

        stats.setTotalApplications(totalApps);
        stats.setPendingApplications(pendingApps);
        stats.setShortlistedCandidates(shortlisted);

        return stats;
    }
}