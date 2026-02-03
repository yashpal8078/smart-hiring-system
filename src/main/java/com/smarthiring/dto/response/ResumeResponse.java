package com.smarthiring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumeResponse {

    private Long id;
    private Long candidateId;
    private String fileName;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private String downloadUrl;
    private Boolean isPrimary;
    private List<String> extractedSkills;  // Parsed
    private String extractedExperience;
    private String extractedEducation;
    private LocalDateTime uploadedAt;
}