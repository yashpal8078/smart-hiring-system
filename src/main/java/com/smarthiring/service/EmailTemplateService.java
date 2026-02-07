package com.smarthiring.service;

import com.smarthiring.config.EmailConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService {

    private final EmailConfig emailConfig;

    /**
     * Generate Welcome Email
     */
    public String generateWelcomeEmail(String userName, String role) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #2563eb; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #2563eb; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Welcome to Smart Hiring System!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Thank you for registering with Smart Hiring System as a <strong>%s</strong>.</p>
                        <p>You can now:</p>
                        <ul>
                            %s
                        </ul>
                        <a href="%s" class="button">Get Started</a>
                        <p>If you have any questions, feel free to contact our support team.</p>
                        <p>Best regards,<br>Smart Hiring Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Smart Hiring System. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                userName,
                role,
                role.equals("CANDIDATE") ?
                        "<li>Browse and apply for jobs</li><li>Upload your resume</li><li>Track your applications</li>" :
                        "<li>Post new job openings</li><li>Review applications</li><li>Shortlist candidates</li>",
                emailConfig.getBaseUrl()
        );
    }

    /**
     * Generate Application Received Email (for HR)
     */
    public String generateApplicationReceivedEmail(String hrName, String candidateName,
                                                   String jobTitle, String appliedDate) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #059669; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .info-box { background: white; padding: 15px; border-radius: 6px; margin: 15px 0; border-left: 4px solid #059669; }
                    .button { display: inline-block; background: #059669; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📩 New Application Received!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>A new application has been submitted for your job posting.</p>
                        
                        <div class="info-box">
                            <p><strong>Job:</strong> %s</p>
                            <p><strong>Candidate:</strong> %s</p>
                            <p><strong>Applied On:</strong> %s</p>
                        </div>
                        
                        <p>Log in to review the application and check the AI-generated match score.</p>
                        
                        <a href="%s/dashboard/applications" class="button">Review Application</a>
                        
                        <p>Best regards,<br>Smart Hiring System</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Smart Hiring System. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(hrName, jobTitle, candidateName, appliedDate, emailConfig.getBaseUrl());
    }

    /**
     * Generate Application Confirmation Email (for Candidate)
     */
    public String generateApplicationConfirmationEmail(String candidateName, String jobTitle,
                                                       String companyName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #2563eb; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .info-box { background: white; padding: 15px; border-radius: 6px; margin: 15px 0; border-left: 4px solid #2563eb; }
                    .button { display: inline-block; background: #2563eb; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Application Submitted!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Your application has been successfully submitted!</p>
                        
                        <div class="info-box">
                            <p><strong>Position:</strong> %s</p>
                            <p><strong>Company:</strong> %s</p>
                            <p><strong>Status:</strong> Under Review</p>
                        </div>
                        
                        <p>We will notify you once there's an update on your application.</p>
                        
                        <a href="%s/my-applications" class="button">Track Application</a>
                        
                        <p>Good luck!<br>Smart Hiring Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Smart Hiring System. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(candidateName, jobTitle, companyName, emailConfig.getBaseUrl());
    }

    /**
     * Generate Status Update Email
     */
    public String generateStatusUpdateEmail(String candidateName, String jobTitle,
                                            String oldStatus, String newStatus) {
        String statusColor = getStatusColor(newStatus);
        String statusMessage = getStatusMessage(newStatus);

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: %s; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .status-badge { display: inline-block; background: %s; color: white; padding: 8px 16px; border-radius: 20px; font-weight: bold; }
                    .info-box { background: white; padding: 15px; border-radius: 6px; margin: 15px 0; }
                    .button { display: inline-block; background: #2563eb; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📋 Application Status Update</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>There's an update on your application for <strong>%s</strong>.</p>
                        
                        <div class="info-box" style="text-align: center;">
                            <p>Your application status has been updated to:</p>
                            <span class="status-badge">%s</span>
                        </div>
                        
                        <p>%s</p>
                        
                        <a href="%s/my-applications" class="button">View Details</a>
                        
                        <p>Best regards,<br>Smart Hiring Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Smart Hiring System. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                statusColor, statusColor, candidateName, jobTitle,
                newStatus, statusMessage, emailConfig.getBaseUrl()
        );
    }

    /**
     * Generate Interview Scheduled Email
     */
    public String generateInterviewEmail(String candidateName, String jobTitle,
                                         LocalDateTime interviewDate, String remarks) {
        String formattedDate = interviewDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
        String formattedTime = interviewDate.format(DateTimeFormatter.ofPattern("hh:mm a"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #7c3aed; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .interview-box { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border: 2px solid #7c3aed; }
                    .interview-box h3 { color: #7c3aed; margin-top: 0; }
                    .detail-row { display: flex; margin: 10px 0; }
                    .detail-label { font-weight: bold; width: 100px; }
                    .button { display: inline-block; background: #7c3aed; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🗓️ Interview Scheduled!</h1>
                    </div>
                    <div class="content">
                        <h2>Congratulations %s!</h2>
                        <p>Your interview for <strong>%s</strong> has been scheduled.</p>
                        
                        <div class="interview-box">
                            <h3>📅 Interview Details</h3>
                            <p><strong>Date:</strong> %s</p>
                            <p><strong>Time:</strong> %s</p>
                            %s
                        </div>
                        
                        <h3>📝 Tips for Your Interview:</h3>
                        <ul>
                            <li>Research the company and role thoroughly</li>
                            <li>Prepare examples of your relevant experience</li>
                            <li>Have questions ready to ask the interviewer</li>
                            <li>Test your tech setup if it's a video interview</li>
                            <li>Join 5 minutes early</li>
                        </ul>
                        
                        <a href="%s/my-applications" class="button">View Application</a>
                        
                        <p>Good luck! 🍀<br>Smart Hiring Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Smart Hiring System. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                candidateName, jobTitle, formattedDate, formattedTime,
                remarks != null ? "<p><strong>Notes:</strong> " + remarks + "</p>" : "",
                emailConfig.getBaseUrl()
        );
    }

    /**
     * Generate Shortlisted Email
     */
    public String generateShortlistedEmail(String candidateName, String jobTitle) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #059669; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .congrats-box { background: #d1fae5; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: center; }
                    .button { display: inline-block; background: #059669; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Congratulations!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        
                        <div class="congrats-box">
                            <h2 style="color: #059669; margin: 0;">You've Been Shortlisted! 🌟</h2>
                        </div>
                        
                        <p>Great news! You have been shortlisted for the position of <strong>%s</strong>.</p>
                        
                        <p>This means your profile and skills closely match what we're looking for. Our team will contact you soon with next steps, which may include:</p>
                        
                        <ul>
                            <li>Technical assessment</li>
                            <li>Phone/Video interview</li>
                            <li>On-site interview</li>
                        </ul>
                        
                        <p>Please ensure your contact information is up to date.</p>
                        
                        <a href="%s/my-applications" class="button">View Application</a>
                        
                        <p>Best of luck!<br>Smart Hiring Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Smart Hiring System. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(candidateName, jobTitle, emailConfig.getBaseUrl());
    }

    /**
     * Generate Password Reset Email
     */
    public String generatePasswordResetEmail(String userName, String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #dc2626; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #dc2626; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .warning { background: #fef2f2; border: 1px solid #fecaca; padding: 15px; border-radius: 6px; margin: 15px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Password Reset Request</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>We received a request to reset your password.</p>
                        
                        <p>Click the button below to reset your password:</p>
                        
                        <a href="%s" class="button">Reset Password</a>
                        
                        <div class="warning">
                            <p><strong>⚠️ Important:</strong></p>
                            <ul>
                                <li>This link expires in 1 hour</li>
                                <li>If you didn't request this, ignore this email</li>
                                <li>Never share this link with anyone</li>
                            </ul>
                        </div>
                        
                        <p>If you didn't request a password reset, please contact our support team.</p>
                        
                        <p>Best regards,<br>Smart Hiring Team</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Smart Hiring System. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, resetLink);
    }

    /**
     * Get status color based on application status
     */
    private String getStatusColor(String status) {
        return switch (status.toUpperCase()) {
            case "APPLIED", "UNDER_REVIEW" -> "#3b82f6"; // Blue
            case "SHORTLISTED" -> "#059669"; // Green
            case "INTERVIEW_SCHEDULED", "INTERVIEWED" -> "#7c3aed"; // Purple
            case "OFFERED", "HIRED" -> "#10b981"; // Emerald
            case "REJECTED" -> "#dc2626"; // Red
            case "WITHDRAWN" -> "#6b7280"; // Gray
            default -> "#2563eb"; // Default blue
        };
    }

    /**
     * Get status message based on application status
     */
    private String getStatusMessage(String status) {
        return switch (status.toUpperCase()) {
            case "UNDER_REVIEW" -> "Your application is being reviewed by the hiring team.";
            case "SHORTLISTED" -> "Congratulations! You've been shortlisted for further evaluation.";
            case "INTERVIEW_SCHEDULED" -> "An interview has been scheduled. Check your email for details.";
            case "INTERVIEWED" -> "Thank you for completing the interview. We'll be in touch soon.";
            case "OFFERED" -> "🎉 Congratulations! You've received a job offer!";
            case "HIRED" -> "Welcome aboard! We're excited to have you on the team!";
            case "REJECTED" -> "Thank you for your interest. Unfortunately, we've decided to move forward with other candidates.";
            case "WITHDRAWN" -> "Your application has been withdrawn as requested.";
            default -> "There's an update on your application.";
        };
    }
}