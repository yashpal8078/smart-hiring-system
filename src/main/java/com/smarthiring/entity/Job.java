package com.smarthiring.entity;

import com.smarthiring.enums.JobType;
import com.smarthiring.enums.WorkMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Job title is required")
    @Size(min = 3, max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Job description is required")
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    @NotBlank(message = "Required skills are required")
    @Column(name = "required_skills", columnDefinition = "TEXT", nullable = false)
    private String requiredSkills;  // Comma-separated

    @Column(name = "nice_to_have_skills", columnDefinition = "TEXT")
    private String niceToHaveSkills;  // Comma-separated

    @Column(name = "experience_min")
    @Builder.Default
    private Integer experienceMin = 0;

    @Column(name = "experience_max")
    private Integer experienceMax;

    @Column(name = "salary_min", precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "salary_currency", length = 10)
    @Builder.Default
    private String salaryCurrency = "INR";

    @Size(max = 100)
    @Column(name = "location", length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 20)
    @Builder.Default
    private JobType jobType = JobType.FULL_TIME;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", length = 20)
    @Builder.Default
    private WorkMode workMode = WorkMode.ONSITE;

    @Size(max = 100)
    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "openings")
    @Builder.Default
    private Integer openings = 1;

    // Many-to-One relationship with User (HR who posted)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by", nullable = false)
    private User postedBy;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "views_count")
    @Builder.Default
    private Integer viewsCount = 0;

    @Column(name = "applications_count")
    @Builder.Default
    private Integer applicationsCount = 0;

    // One-to-Many relationship with Application
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Application> applications = new ArrayList<>();

    // One-to-Many relationship with Shortlist
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Shortlist> shortlists = new ArrayList<>();

    // Helper method to increment view count
    public void incrementViewCount() {
        this.viewsCount++;
    }

    // Helper method to increment application count
    public void incrementApplicationCount() {
        this.applicationsCount++;
    }

    // Check if job is expired
    public boolean isExpired() {
        if (applicationDeadline == null) {
            return false;
        }
        return LocalDate.now().isAfter(applicationDeadline);
    }
}