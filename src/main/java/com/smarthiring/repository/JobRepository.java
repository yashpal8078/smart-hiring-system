package com.smarthiring.repository;

import com.smarthiring.entity.Job;
import com.smarthiring.enums.JobType;
import com.smarthiring.enums.WorkMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    /**
     * Find all active jobs
     */
    Page<Job> findByIsActiveTrue(Pageable pageable);

    /**
     * Find all active jobs as list
     */
    List<Job> findByIsActiveTrue();

    /**
     * Find jobs posted by a user
     */
    Page<Job> findByPostedById(Long userId, Pageable pageable);

    /**
     * Find jobs by location
     */
    Page<Job> findByLocationIgnoreCaseAndIsActiveTrue(String location, Pageable pageable);

    /**
     * Find jobs by job type
     */
    Page<Job> findByJobTypeAndIsActiveTrue(JobType jobType, Pageable pageable);

    /**
     * Find jobs by work mode
     */
    Page<Job> findByWorkModeAndIsActiveTrue(WorkMode workMode, Pageable pageable);

    /**
     * Find featured jobs
     */
    List<Job> findByIsFeaturedTrueAndIsActiveTrue();

    /**
     * Search jobs by title or description (Full-text search)
     */
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
            "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> searchJobs(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Advanced job search with multiple filters
     */
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
            "(:keyword IS NULL OR " +
            "   LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "   LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:location IS NULL OR LOWER(j.location) = LOWER(:location)) AND " +
            "(:jobType IS NULL OR j.jobType = :jobType) AND " +
            "(:workMode IS NULL OR j.workMode = :workMode) AND " +
            "(:minExp IS NULL OR j.experienceMin >= :minExp) AND " +
            "(:maxExp IS NULL OR j.experienceMax <= :maxExp)")
    Page<Job> advancedSearch(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("jobType") JobType jobType,
            @Param("workMode") WorkMode workMode,
            @Param("minExp") Integer minExp,
            @Param("maxExp") Integer maxExp,
            Pageable pageable
    );

    /**
     * Find jobs matching candidate experience
     */
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
            "j.experienceMin <= :experience AND " +
            "(j.experienceMax IS NULL OR j.experienceMax >= :experience)")
    Page<Job> findJobsForExperience(@Param("experience") Integer experience, Pageable pageable);

    /**
     * Find jobs by required skill
     */
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
            "LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :skill, '%'))")
    Page<Job> findBySkill(@Param("skill") String skill, Pageable pageable);

    /**
     * Find jobs expiring soon
     */
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
            "j.applicationDeadline BETWEEN :today AND :endDate")
    List<Job> findJobsExpiringSoon(
            @Param("today") LocalDate today,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find expired jobs
     */
    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
            "j.applicationDeadline < :today")
    List<Job> findExpiredJobs(@Param("today") LocalDate today);

    /**
     * Increment view count
     */
    @Modifying
    @Query("UPDATE Job j SET j.viewsCount = j.viewsCount + 1 WHERE j.id = :jobId")
    int incrementViewCount(@Param("jobId") Long jobId);

    /**
     * Increment application count
     */
    @Modifying
    @Query("UPDATE Job j SET j.applicationsCount = j.applicationsCount + 1 WHERE j.id = :jobId")
    int incrementApplicationCount(@Param("jobId") Long jobId);

    /**
     * Deactivate expired jobs
     */
    @Modifying
    @Query("UPDATE Job j SET j.isActive = false WHERE j.applicationDeadline < :today AND j.isActive = true")
    int deactivateExpiredJobs(@Param("today") LocalDate today);

    /**
     * Count active jobs
     */
    long countByIsActiveTrue();

    /**
     * Count jobs by posted user
     */
    long countByPostedById(Long userId);

    /**
     * Get all distinct locations
     */
    @Query("SELECT DISTINCT j.location FROM Job j WHERE j.location IS NOT NULL AND j.isActive = true ORDER BY j.location")
    List<String> findAllDistinctLocations();

    /**
     * Get all distinct departments
     */
    @Query("SELECT DISTINCT j.department FROM Job j WHERE j.department IS NOT NULL AND j.isActive = true ORDER BY j.department")
    List<String> findAllDistinctDepartments();

    /**
     * Find recent jobs
     */
    @Query("SELECT j FROM Job j WHERE j.isActive = true ORDER BY j.createdAt DESC")
    List<Job> findRecentJobs(Pageable pageable);

    /**
     * Find most applied jobs
     */
    @Query("SELECT j FROM Job j WHERE j.isActive = true ORDER BY j.applicationsCount DESC")
    List<Job> findMostAppliedJobs(Pageable pageable);
}