package com.smarthiring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsResponse {

    // For Admin/HR
    private Long totalJobs;
    private Long activeJobs;
    private Long totalCandidates;
    private Long totalApplications;
    private Long pendingApplications;
    private Long shortlistedCandidates;
    private Long hiredCandidates;

    // For Candidates
    private Long appliedJobs;
    private Long shortlistedJobs;
    private Long interviewsScheduled;

    // Charts data
    private List<Map<String, Object>> applicationsByStatus;
    private List<Map<String, Object>> applicationsByDate;
    private List<Map<String, Object>> jobsByDepartment;
    private List<Map<String, Object>> topJobs;

    // Recent items
    private List<ApplicationResponse> recentApplications;
    private List<JobListResponse> recentJobs;
}