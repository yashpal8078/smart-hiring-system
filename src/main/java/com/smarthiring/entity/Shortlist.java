package com.smarthiring.entity;

import com.smarthiring.enums.ShortlistStage;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shortlists",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"job_id", "candidate_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shortlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One relationship with Job
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    // Many-to-One relationship with Candidate
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    // Many-to-One relationship with Application
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    // Many-to-One relationship with User (HR who shortlisted)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shortlisted_by", nullable = false)
    private User shortlistedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", length = 20)
    @Builder.Default
    private ShortlistStage stage = ShortlistStage.INITIAL;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "interview_date")
    private LocalDateTime interviewDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}