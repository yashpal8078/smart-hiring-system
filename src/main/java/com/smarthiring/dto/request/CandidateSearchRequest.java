package com.smarthiring.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateSearchRequest {

    private String skills;
    private String location;
    private BigDecimal minExperience;
    private BigDecimal maxExperience;
    private Integer maxNoticePeriod;

    // Pagination
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}