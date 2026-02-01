package com.smarthiring.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One-to-One relationship with User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Size(max = 200)
    @Column(name = "headline", length = 200)
    private String headline;  // e.g., "Senior Java Developer with 5+ years experience"

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;  // Comma-separated skills

    @Column(name = "total_experience", precision = 4, scale = 2)
    private BigDecimal totalExperience;  // Years of experience

    @Size(max = 100)
    @Column(name = "current_company", length = 100)
    private String currentCompany;

    @Size(max = 100)
    @Column(name = "current_designation", length = 100)
    private String currentDesignation;

    @Column(name = "current_salary", precision = 12, scale = 2)
    private BigDecimal currentSalary;

    @Column(name = "expected_salary", precision = 12, scale = 2)
    private BigDecimal expectedSalary;

    @Column(name = "notice_period")
    private Integer noticePeriod;  // In days

    @Size(max = 100)
    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "preferred_locations")
    private String preferredLocations;  // Comma-separated

    @Column(name = "education")
    private String education;

    @Size(max = 255)
    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Size(max = 255)
    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Size(max = 255)
    @Column(name = "portfolio_url", length = 255)
    private String portfolioUrl;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @Column(name = "gender", length = 20)
    private String gender;

    // One-to-Many relationship with Resume
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Resume> resumes = new ArrayList<>();

    // One-to-Many relationship with Application
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Application> applications = new ArrayList<>();

    // Helper method to add resume
    public void addResume(Resume resume) {
        resumes.add(resume);
        resume.setCandidate(this);
    }

    // Helper method to remove resume
    public void removeResume(Resume resume) {
        resumes.remove(resume);
        resume.setCandidate(null);
    }

    // Get primary resume
    public Resume getPrimaryResume() {
        return resumes.stream()
                .filter(Resume::getIsPrimary)
                .findFirst()
                .orElse(resumes.isEmpty() ? null : resumes.get(0));
    }
}