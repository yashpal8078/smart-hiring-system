package com.smarthiring.repository;

import com.smarthiring.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    /**
     * Find all resumes by candidate ID
     */
    List<Resume> findByCandidateId(Long candidateId);

    /**
     * Find primary resume by candidate ID
     */
    Optional<Resume> findByCandidateIdAndIsPrimaryTrue(Long candidateId);

    /**
     * Find resume by file name
     */
    Optional<Resume> findByFileName(String fileName);

    /**
     * Count resumes by candidate ID
     */
    long countByCandidateId(Long candidateId);

    /**
     * Check if candidate has any resume
     */
    boolean existsByCandidateId(Long candidateId);

    /**
     * Set all resumes as non-primary for a candidate
     */
    @Modifying
    @Query("UPDATE Resume r SET r.isPrimary = false WHERE r.candidate.id = :candidateId")
    int resetPrimaryResume(@Param("candidateId") Long candidateId);

    /**
     * Set specific resume as primary
     */
    @Modifying
    @Query("UPDATE Resume r SET r.isPrimary = true WHERE r.id = :resumeId")
    int setPrimaryResume(@Param("resumeId") Long resumeId);

    /**
     * Delete all resumes by candidate ID
     */
    void deleteByCandidateId(Long candidateId);

    /**
     * Find resumes by file type
     */
    List<Resume> findByFileType(String fileType);

    /**
     * Find resumes with extracted skills containing keyword
     */
    @Query("SELECT r FROM Resume r WHERE LOWER(r.extractedSkills) LIKE LOWER(CONCAT('%', :skill, '%'))")
    List<Resume> findByExtractedSkillContaining(@Param("skill") String skill);

    /**
     * Find resumes without parsed text (needs parsing)
     */
    @Query("SELECT r FROM Resume r WHERE r.parsedText IS NULL OR r.parsedText = ''")
    List<Resume> findResumesNeedingParsing();
}