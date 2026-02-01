package com.smarthiring.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-One relationship with Candidate
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @NotBlank(message = "File name is required")
    @Column(name = "file_name", nullable = false)
    private String fileName;  // UUID generated name

    @NotBlank(message = "Original file name is required")
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;  // User's original file name

    @NotBlank(message = "File path is required")
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_type", length = 50)
    private String fileType;  // pdf, doc, docx

    @Column(name = "file_size")
    private Long fileSize;  // Size in bytes

    @Column(name = "parsed_text", columnDefinition = "LONGTEXT")
    private String parsedText;  // Extracted text from resume

    @Column(name = "extracted_skills", columnDefinition = "TEXT")
    private String extractedSkills;  // AI extracted skills

    @Column(name = "extracted_experience", columnDefinition = "TEXT")
    private String extractedExperience;  // AI extracted experience

    @Column(name = "extracted_education", columnDefinition = "TEXT")
    private String extractedEducation;  // AI extracted education

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}