package com.smarthiring.service;

import com.smarthiring.dto.response.NotificationResponse;
import com.smarthiring.dto.response.PagedResponse;
import com.smarthiring.entity.*;
import com.smarthiring.enums.ApplicationStatus;
import com.smarthiring.enums.NotificationType;
import com.smarthiring.exception.ResourceNotFoundException;
import com.smarthiring.repository.NotificationRepository;
import com.smarthiring.repository.UserRepository;
import com.smarthiring.util.AppConstants;
import com.smarthiring.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Get notifications for a user
     */
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Notification> notificationsPage = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<NotificationResponse> content = notificationsPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                notificationsPage.getNumber(),
                notificationsPage.getSize(),
                notificationsPage.getTotalElements(),
                notificationsPage.getTotalPages()
        );
    }

    /**
     * Get unread notifications
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Count unread notifications
     */
    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    /**
     * Mark all notifications as read
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    /**
     * Send notification for new application received
     */
    @Transactional
    public void sendApplicationReceivedNotification(Job job, Candidate candidate) {
        Notification notification = Notification.builder()
                .user(job.getPostedBy())
                .title("New Application Received")
                .message(String.format(AppConstants.NOTIFICATION_APPLICATION_RECEIVED, job.getTitle()) +
                        " from " + candidate.getUser().getFullName())
                .type(NotificationType.APPLICATION_RECEIVED)
                .referenceId(job.getId())
                .referenceType("JOB")
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        log.debug("Application received notification sent to HR: {}", job.getPostedBy().getId());
    }

    /**
     * Send notification for application status update
     */
    @Transactional
    public void sendStatusUpdateNotification(Application application, ApplicationStatus oldStatus, ApplicationStatus newStatus) {
        Notification notification = Notification.builder()
                .user(application.getCandidate().getUser())
                .title("Application Status Updated")
                .message(String.format(AppConstants.NOTIFICATION_APPLICATION_STATUS, newStatus.getDisplayName()) +
                        " for " + application.getJob().getTitle())
                .type(NotificationType.APPLICATION_STATUS)
                .referenceId(application.getId())
                .referenceType("APPLICATION")
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        log.debug("Status update notification sent to candidate: {}", application.getCandidate().getUser().getId());
    }

    /**
     * Send notification for shortlist
     */
    @Transactional
    public void sendShortlistNotification(Shortlist shortlist) {
        Notification notification = Notification.builder()
                .user(shortlist.getCandidate().getUser())
                .title("Congratulations! You've Been Shortlisted")
                .message(String.format(AppConstants.NOTIFICATION_SHORTLISTED, shortlist.getJob().getTitle()))
                .type(NotificationType.APPLICATION_STATUS)
                .referenceId(shortlist.getId())
                .referenceType("SHORTLIST")
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        log.debug("Shortlist notification sent to candidate: {}", shortlist.getCandidate().getUser().getId());
    }

    /**
     * Send notification for interview scheduled
     */
    @Transactional
    public void sendInterviewNotification(Shortlist shortlist) {
        if (shortlist.getInterviewDate() == null) {
            return;
        }

        Notification notification = Notification.builder()
                .user(shortlist.getCandidate().getUser())
                .title("Interview Scheduled")
                .message(String.format(AppConstants.NOTIFICATION_INTERVIEW,
                        shortlist.getJob().getTitle(),
                        shortlist.getInterviewDate().toString()))
                .type(NotificationType.INTERVIEW_SCHEDULED)
                .referenceId(shortlist.getId())
                .referenceType("SHORTLIST")
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        log.debug("Interview notification sent to candidate: {}", shortlist.getCandidate().getUser().getId());
    }

    /**
     * Create custom notification
     */
    @Transactional
    public void createNotification(Long userId, String title, String message, NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * Delete old notifications (cleanup task)
     */
    @Transactional
    public int deleteOldNotifications(int daysOld) {
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(daysOld);
        int count = notificationRepository.deleteOldReadNotifications(beforeDate);
        if (count > 0) {
            log.info("Deleted {} old notifications", count);
        }
        return count;
    }

    /**
     * Convert entity to response
     */
    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .typeDisplay(notification.getType().name())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .timeAgo(DateUtils.getTimeAgo(notification.getCreatedAt()))
                .build();
    }
}