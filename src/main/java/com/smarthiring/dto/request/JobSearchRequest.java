package com.smarthiring.dto.request;

import com.smarthiring.enums.JobType;
import com.smarthiring.enums.WorkMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSearchRequest {

    private String keyword;
    private String location;
    private JobType jobType;
    private WorkMode workMode;
    private Integer minExperience;
    private Integer maxExperience;
    private String skills;

    // Pagination
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}