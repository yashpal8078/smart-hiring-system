package com.smarthiring.util;

public final class AppConstants {

    private AppConstants() {

    }

    // Pagination defaults
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    // File upload
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String[] ALLOWED_RESUME_TYPES = {"pdf", "doc", "docx"};
    public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png"};

    // AI Scoring
    public static final double MIN_AI_SCORE = 0.0;
    public static final double MAX_AI_SCORE = 100.0;
    public static final double SHORTLIST_THRESHOLD = 70.0;

    // Notification messages
    public static final String NOTIFICATION_APPLICATION_RECEIVED = "New application received for %s";
    public static final String NOTIFICATION_APPLICATION_STATUS = "Your application status has been updated to %s";
    public static final String NOTIFICATION_SHORTLISTED = "Congratulations! You have been shortlisted for %s";
    public static final String NOTIFICATION_INTERVIEW = "Interview scheduled for %s on %s";
}