package com.smarthiring.repository;

import com.smarthiring.entity.Shortlist;
import com.smarthiring.enums.ShortlistStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShortlistRepository extends JpaRepository<Shortlist, Long> {

    /**
     * Find shortlists by job ID
     */
    Page<Shortlist> findByJobId(Long jobId, Pageable pageable);

    /**
     * Find shortlists by job ID as list
     */
    List<Shortlist> findByJobId(Long jobId);

    /**
     * Find shortlists by candidate ID
     */
    List<Shortlist> findByCandidateId(Long candidateId);

    /**
     * Find shortlist by job and candidate
     */
    Optional<Shortlist> findByJobIdAndCandidateId(Long jobId, Long candidateId);

    /**
     * Check if candidate is shortlisted for job
     */
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    /**
     * Find shortlists by stage
     */
    Page<Shortlist> findByStage(ShortlistStage stage, Pageable pageable);

    /**
     * Find shortlists by job and stage
     */
    List<Shortlist> findByJobIdAndStage(Long jobId, ShortlistStage stage);

    /**
     * Find shortlists by HR who shortlisted
     */
    Page<Shortlist> findByShortlistedById(Long userId, Pageable pageable);

    /**
     * Update shortlist stage
     */
    @Modifying
    @Query("UPDATE Shortlist s SET s.stage = :stage WHERE s.id = :shortlistId")
    int updateStage(@Param("shortlistId") Long shortlistId, @Param("stage") ShortlistStage stage);

    /**
     * Update interview date
     */
    @Modifying
    @Query("UPDATE Shortlist s SET s.interviewDate = :interviewDate WHERE s.id = :shortlistId")
    int updateInterviewDate(@Param("shortlistId") Long shortlistId, @Param("interviewDate") LocalDateTime interviewDate);

    /**
     * Count shortlists by job
     */
    long countByJobId(Long jobId);

    /**
     * Count shortlists by job and stage
     */
    long countByJobIdAndStage(Long jobId, ShortlistStage stage);

    /**
     * Find upcoming interviews
     */
    @Query("SELECT s FROM Shortlist s WHERE s.interviewDate BETWEEN :startDate AND :endDate ORDER BY s.interviewDate")
    List<Shortlist> findUpcomingInterviews(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find today's interviews
     */
    @Query("SELECT s FROM Shortlist s WHERE DATE(s.interviewDate) = DATE(:today) ORDER BY s.interviewDate")
    List<Shortlist> findTodayInterviews(@Param("today") LocalDateTime today);

    /**
     * Delete by application ID
     */
    void deleteByApplicationId(Long applicationId);
}