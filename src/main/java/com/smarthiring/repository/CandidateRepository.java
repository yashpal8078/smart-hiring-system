package com.smarthiring.repository;

import com.smarthiring.entity.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    /**
     * Find candidate by user ID
     */
    Optional<Candidate> findByUserId(Long userId);

    /**
     * Find candidate by user email
     */
    @Query("SELECT c FROM Candidate c WHERE c.user.email = :email")
    Optional<Candidate> findByUserEmail(@Param("email") String email);

    /**
     * Check if candidate exists for user
     */
    boolean existsByUserId(Long userId);

    /**
     * Find candidates by location
     */
    List<Candidate> findByLocationIgnoreCase(String location);

    /**
     * Find candidates by location with pagination
     */
    Page<Candidate> findByLocationIgnoreCase(String location, Pageable pageable);

    /**
     * Find candidates with experience in range
     */
    @Query("SELECT c FROM Candidate c WHERE c.totalExperience BETWEEN :minExp AND :maxExp")
    Page<Candidate> findByExperienceRange(
            @Param("minExp") BigDecimal minExp,
            @Param("maxExp") BigDecimal maxExp,
            Pageable pageable
    );

    /**
     * Find candidates with minimum experience
     */
    @Query("SELECT c FROM Candidate c WHERE c.totalExperience >= :minExp")
    Page<Candidate> findByMinExperience(@Param("minExp") BigDecimal minExp, Pageable pageable);

    /**
     * Search candidates by skills (contains)
     */
    @Query("SELECT c FROM Candidate c WHERE LOWER(c.skills) LIKE LOWER(CONCAT('%', :skill, '%'))")
    Page<Candidate> findBySkillContaining(@Param("skill") String skill, Pageable pageable);

    /**
     * Search candidates by multiple criteria
     */
    @Query("SELECT c FROM Candidate c WHERE " +
            "(:location IS NULL OR LOWER(c.location) = LOWER(:location)) AND " +
            "(:minExp IS NULL OR c.totalExperience >= :minExp) AND " +
            "(:maxExp IS NULL OR c.totalExperience <= :maxExp) AND " +
            "(:skill IS NULL OR LOWER(c.skills) LIKE LOWER(CONCAT('%', :skill, '%')))")
    Page<Candidate> searchCandidates(
            @Param("location") String location,
            @Param("minExp") BigDecimal minExp,
            @Param("maxExp") BigDecimal maxExp,
            @Param("skill") String skill,
            Pageable pageable
    );

    /**
     * Find candidates with expected salary in range
     */
    @Query("SELECT c FROM Candidate c WHERE c.expectedSalary BETWEEN :minSalary AND :maxSalary")
    List<Candidate> findByExpectedSalaryRange(
            @Param("minSalary") BigDecimal minSalary,
            @Param("maxSalary") BigDecimal maxSalary
    );

    /**
     * Find candidates available within notice period
     */
    @Query("SELECT c FROM Candidate c WHERE c.noticePeriod <= :maxNoticePeriod")
    List<Candidate> findByMaxNoticePeriod(@Param("maxNoticePeriod") Integer maxNoticePeriod);

    /**
     * Count candidates by location
     */
    long countByLocationIgnoreCase(String location);

    /**
     * Get all distinct locations
     */
    @Query("SELECT DISTINCT c.location FROM Candidate c WHERE c.location IS NOT NULL ORDER BY c.location")
    List<String> findAllDistinctLocations();

    /**
     * Find candidates who haven't applied to a specific job
     */
    @Query("SELECT c FROM Candidate c WHERE c.id NOT IN " +
            "(SELECT a.candidate.id FROM Application a WHERE a.job.id = :jobId)")
    Page<Candidate> findCandidatesNotAppliedToJob(@Param("jobId") Long jobId, Pageable pageable);
}