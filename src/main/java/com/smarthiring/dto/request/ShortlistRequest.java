package com.smarthiring.dto.request;

import com.smarthiring.enums.ShortlistStage;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortlistRequest {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    private ShortlistStage stage;

    @Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    private String remarks;

    private LocalDateTime interviewDate;
}