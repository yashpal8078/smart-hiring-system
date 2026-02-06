package com.smarthiring.controller;

import com.smarthiring.dto.response.ApiResponse;
import com.smarthiring.dto.response.NotificationResponse;
import com.smarthiring.dto.response.PagedResponse;
import com.smarthiring.security.CustomUserDetails;
import com.smarthiring.service.NotificationService;
import com.smarthiring.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "User Notification APIs")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get my notifications
     */
    @GetMapping
    @Operation(summary = "Get My Notifications", description = "Get all notifications for current user")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Get notifications for user: {}", userDetails.getEmail());

        PagedResponse<NotificationResponse> response = notificationService.getNotifications(
                userDetails.getId(), page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get unread notifications
     */
    @GetMapping("/unread")
    @Operation(summary = "Get Unread Notifications", description = "Get all unread notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Get unread notifications for user: {}", userDetails.getEmail());

        List<NotificationResponse> response = notificationService.getUnreadNotifications(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get unread count
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Get Unread Count", description = "Get count of unread notifications")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Get unread count for user: {}", userDetails.getEmail());

        long count = notificationService.countUnreadNotifications(userDetails.getId());

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Mark notification as read
     */
    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark as Read", description = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id
    ) {
        log.info("Mark notification {} as read", id);

        notificationService.markAsRead(id);

        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    /**
     * Mark all notifications as read
     */
    @PatchMapping("/read-all")
    @Operation(summary = "Mark All as Read", description = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Mark all notifications as read for user: {}", userDetails.getEmail());

        notificationService.markAllAsRead(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }
}