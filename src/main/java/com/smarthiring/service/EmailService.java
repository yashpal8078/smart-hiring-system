package com.smarthiring.service;

import com.smarthiring.config.EmailConfig;
import com.smarthiring.dto.EmailDto;
import com.smarthiring.entity.Application;
import com.smarthiring.entity.Shortlist;
import com.smarthiring.entity.User;
import com.smarthiring.enums.ApplicationStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailConfig emailConfig;
    private final EmailTemplateService templateService;

    /**
     * Send email asynchronously
     */
    @Async("emailExecutor")
    public void sendEmail(EmailDto emailDto) {
        if (!emailConfig.isEnabled()) {
            log.info("Email sending is disabled. Would have sent email to: {}", emailDto.getTo());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailConfig.getFrom(), emailConfig.getFromName());
            helper.setTo(emailDto.getTo());
            helper.setSubject(emailDto.getSubject());
            helper.setText(emailDto.getBody(), emailDto.isHtml());

            mailSender.send(message);

            log.info("Email sent successfully to: {}", emailDto.getTo());

        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to {}: {}", emailDto.getTo(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending email: {}", e.getMessage());
        }
    }

    /**
     * Send welcome email to new user
     */
    @Async("emailExecutor")
    public void sendWelcomeEmail(User user, String role) {
        String roleName = role.replace("ROLE_", "");
        String body = templateService.generateWelcomeEmail(user.getFullName(), roleName);

        EmailDto email = EmailDto.builder()
                .to(user.getEmail())
                .toName(user.getFullName())
                .subject("Welcome to Smart Hiring System! 🎉")
                .body(body)
                .isHtml(true)
                .build();

        sendEmail(email);
    }

    /**
     * Send application received email to HR
     */
    @Async("emailExecutor")
    public void sendApplicationReceivedEmailToHR(Application application) {
        User hr = application.getJob().getPostedBy();
        String candidateName = application.getCandidate().getUser().getFullName();
        String jobTitle = application.getJob().getTitle();
        String appliedDate = application.getAppliedAt()
                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));

        String body = templateService.generateApplicationReceivedEmail(
                hr.getFullName(), candidateName, jobTitle, appliedDate);

        EmailDto email = EmailDto.builder()
                .to(hr.getEmail())
                .toName(hr.getFullName())
                .subject("New Application: " + jobTitle + " - " + candidateName)
                .body(body)
                .isHtml(true)
                .build();

        sendEmail(email);
    }

    /**
     * Send application confirmation email to candidate
     */
    @Async("emailExecutor")
    public void sendApplicationConfirmationEmail(Application application) {
        User candidate = application.getCandidate().getUser();
        String jobTitle = application.getJob().getTitle();
        String companyName = application.getJob().getPostedBy().getFullName();

        String body = templateService.generateApplicationConfirmationEmail(
                candidate.getFullName(), jobTitle, companyName);

        EmailDto email = EmailDto.builder()
                .to(candidate.getEmail())
                .toName(candidate.getFullName())
                .subject("Application Submitted: " + jobTitle)
                .body(body)
                .isHtml(true)
                .build();

        sendEmail(email);
    }

    /**
     * Send status update email to candidate
     */
    @Async("emailExecutor")
    public void sendStatusUpdateEmail(Application application,
                                      ApplicationStatus oldStatus,
                                      ApplicationStatus newStatus) {
        User candidate = application.getCandidate().getUser();
        String jobTitle = application.getJob().getTitle();

        String body = templateService.generateStatusUpdateEmail(
                candidate.getFullName(),
                jobTitle,
                oldStatus.getDisplayName(),
                newStatus.getDisplayName());

        EmailDto email = EmailDto.builder()
                .to(candidate.getEmail())
                .toName(candidate.getFullName())
                .subject("Application Update: " + jobTitle + " - " + newStatus.getDisplayName())
                .body(body)
                .isHtml(true)
                .build();

        sendEmail(email);
    }

    /**
     * Send shortlisted email to candidate
     */
    @Async("emailExecutor")
    public void sendShortlistedEmail(Shortlist shortlist) {
        User candidate = shortlist.getCandidate().getUser();
        String jobTitle = shortlist.getJob().getTitle();

        String body = templateService.generateShortlistedEmail(
                candidate.getFullName(), jobTitle);

        EmailDto email = EmailDto.builder()
                .to(candidate.getEmail())
                .toName(candidate.getFullName())
                .subject("🎉 Congratulations! You've Been Shortlisted - " + jobTitle)
                .body(body)
                .isHtml(true)
                .build();

        sendEmail(email);
    }

    /**
     * Send interview scheduled email to candidate
     */
    @Async("emailExecutor")
    public void sendInterviewScheduledEmail(Shortlist shortlist) {
        if (shortlist.getInterviewDate() == null) {
            return;
        }

        User candidate = shortlist.getCandidate().getUser();
        String jobTitle = shortlist.getJob().getTitle();

        String body = templateService.generateInterviewEmail(
                candidate.getFullName(),
                jobTitle,
                shortlist.getInterviewDate(),
                shortlist.getRemarks());

        EmailDto email = EmailDto.builder()
                .to(candidate.getEmail())
                .toName(candidate.getFullName())
                .subject("🗓️ Interview Scheduled - " + jobTitle)
                .body(body)
                .isHtml(true)
                .build();

        sendEmail(email);
    }

    /**
     * Send password reset email
     */
    @Async("emailExecutor")
    public void sendPasswordResetEmail(User user, String resetToken) {
        String resetLink = emailConfig.getBaseUrl() + "/reset-password?token=" + resetToken;
        String body = templateService.generatePasswordResetEmail(user.getFullName(), resetLink);

        EmailDto email = EmailDto.builder()
                .to(user.getEmail())
                .toName(user.getFullName())
                .subject("Password Reset Request - Smart Hiring System")
                .body(body)
                .isHtml(true)
                .build();

        sendEmail(email);
    }

    /**
     * Send simple text email
     */
    @Async("emailExecutor")
    public void sendSimpleEmail(String to, String subject, String body) {
        EmailDto email = EmailDto.builder()
                .to(to)
                .subject(subject)
                .body(body)
                .isHtml(false)
                .build();

        sendEmail(email);
    }
}