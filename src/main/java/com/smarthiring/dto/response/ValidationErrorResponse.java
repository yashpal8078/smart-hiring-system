package com.smarthiring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorResponse {

    private boolean success = false;
    private String message;
    private Map<String, String> errors;  // Field -> Error message
    private LocalDateTime timestamp;
    private String path;

    public static ValidationErrorResponse of(Map<String, String> errors, String path) {
        return ValidationErrorResponse.builder()
                .success(false)
                .message("Validation failed")
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
}