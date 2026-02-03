package com.smarthiring.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.smarthiring.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private String typeDisplay;
    private Long referenceId;
    private String referenceType;
    private String referenceUrl;  // URL to navigate to
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String timeAgo;  // e.g., "2 hours ago"
}