package com.smarthiring.repository;

import com.smarthiring.entity.Application;
import com.smarthiring.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    /**
     * Find applications by job ID
     */
    Page<Application> findByJobId(Long jobId, Pageable pageable);

    /**
     * Find applications by job ID as list
     */
    List<Application> findByJobId(Long jobId);

    /**
     * Find applications by candidate ID
     */
    Page<Application> findByCandidateId(Long candidateId, Pageable pageable);

    /**
     * Find applications by candidate ID as list
     */
    List<Application> findByCandidateId(Long candidateId);

    /**
     * Find application by job and candidate (unique combination)
     */
    Optional<Application> findByJobIdAndCandidateId(Long jobId, Long candidateId);

    /**
     * Check if candidate already applied to job
     */
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    /**
     * Find applications by status
     */
    Page<Application> findByStatus(ApplicationStatus status, Pageable pageable);

    /**
     * Find applications by job and status
     */
    Page<Application> findByJobIdAndStatus(Long jobId, ApplicationStatus status, Pageable pageable);

    /**
     * Find applications by candidate and status
     */
    List<Application> findByCandidateIdAndStatus(Long candidateId, ApplicationStatus status);

    /**
     * Find applications with AI score above threshold
     */
    @Query("SELECT a FROM Application a WHERE a.job.id = :jobId AND a.aiScore >= :minScore ORDER BY a.aiScore DESC")
    List<Application> findTopCandidatesByScore(
            @Param("jobId") Long jobId,
            @Param("minScore") BigDecimal minScore
    );

    /**
     * Find applications ordered by AI score
     */
    @Query("SELECT a FROM Application a WHERE a.job.id = :jobId ORDER BY a.aiScore DESC NULLS LAST")
    Page<Application> findByJobIdOrderByAiScoreDesc(@Param("jobId") Long jobId, Pageable pageable);

    /**
     * Update application status
     */
    @Modifying
    @Query("UPDATE Application a SET a.status = :status, a.updatedAt = :updatedAt WHERE a.id = :appId")
    int updateStatus(
            @Param("appId") Long appId,
            @Param("status") ApplicationStatus status,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Update AI score and feedback
     */
    @Modifying
    @Query("UPDATE Application a SET a.aiScore = :score, a.aiFeedback = :feedback WHERE a.id = :appId")
    int updateAiScore(
            @Param("appId") Long appId,
            @Param("score") BigDecimal score,
            @Param("feedback") String feedback
    );

    /**
     * Update HR rating and notes
     */
    @Modifying
    @Query("UPDATE Application a SET a.hrRating = :rating, a.hrNotes = :notes, a.updatedAt = :updatedAt WHERE a.id = :appId")
    int updateHrReview(
            @Param("appId") Long appId,
            @Param("rating") Integer rating,
            @Param("notes") String notes,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Count applications by job ID
     */
    long countByJobId(Long jobId);

    /**
     * Count applications by candidate ID
     */
    long countByCandidateId(Long candidateId);

    /**
     * Count applications by status
     */
    long countByStatus(ApplicationStatus status);

    /**
     * Count applications by job and status
     */
    long countByJobIdAndStatus(Long jobId, ApplicationStatus status);

    /**
     * Find applications without AI score (needs scoring)
     */
    @Query("SELECT a FROM Application a WHERE a.aiScore IS NULL")
    List<Application> findApplicationsNeedingScoring();

    /**
     * Find applications by job without AI score
     */
    @Query("SELECT a FROM Application a WHERE a.job.id = :jobId AND a.aiScore IS NULL")
    List<Application> findApplicationsNeedingScoringByJob(@Param("jobId") Long jobId);

    /**
     * Find recent applications
     */
    @Query("SELECT a FROM Application a ORDER BY a.appliedAt DESC")
    List<Application> findRecentApplications(Pageable pageable);

    /**
     * Find recent applications for a job
     */
    @Query("SELECT a FROM Application a WHERE a.job.id = :jobId ORDER BY a.appliedAt DESC")
    List<Application> findRecentApplicationsByJob(@Param("jobId") Long jobId, Pageable pageable);

    /**
     * Find applications applied after date
     */
    List<Application> findByAppliedAtAfter(LocalDateTime date);

    /**
     * Get application statistics by job
     */
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.job.id = :jobId GROUP BY a.status")
    List<Object[]> getApplicationStatsByJob(@Param("jobId") Long jobId);

    /**
     * Get daily application count for last N days
     */
    @Query("SELECT DATE(a.appliedAt), COUNT(a) FROM Application a " +
            "WHERE a.appliedAt >= :startDate GROUP BY DATE(a.appliedAt) ORDER BY DATE(a.appliedAt)")
    List<Object[]> getDailyApplicationCount(@Param("startDate") LocalDateTime startDate);
}