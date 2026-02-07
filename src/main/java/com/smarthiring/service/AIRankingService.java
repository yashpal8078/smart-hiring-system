package com.smarthiring.service;

import com.smarthiring.dto.response.ApplicationResponse;
import com.smarthiring.entity.Application;
import com.smarthiring.entity.Candidate;
import com.smarthiring.entity.Job;
import com.smarthiring.entity.Resume;
import com.smarthiring.enums.ApplicationStatus;
import com.smarthiring.exception.ResourceNotFoundException;
import com.smarthiring.mapper.ApplicationMapper;
import com.smarthiring.repository.ApplicationRepository;
import com.smarthiring.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIRankingService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final SkillMatcherService skillMatcherService;
    private final ApplicationMapper applicationMapper;

    // Scoring weights
    private static final double SKILL_WEIGHT = 0.50;        // 50% weight for skills
    private static final double EXPERIENCE_WEIGHT = 0.25;   // 25% weight for experience
    private static final double EDUCATION_WEIGHT = 0.10;    // 10% weight for education
    private static final double RESUME_QUALITY_WEIGHT = 0.10; // 10% for resume quality
    private static final double RECENCY_WEIGHT = 0.05;      // 5% for application recency

    /**
     * Calculate AI score for an application
     */
    @Transactional
    public BigDecimal calculateApplicationScore(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        return calculateScore(application);
    }

    /**
     * Calculate AI score for an application entity
     */
    public BigDecimal calculateScore(Application application) {
        Job job = application.getJob();
        Candidate candidate = application.getCandidate();
        Resume resume = application.getResume() != null ?
                application.getResume() : candidate.getPrimaryResume();

        double totalScore = 0.0;
        StringBuilder feedback = new StringBuilder();

        // 1. Skill Match Score (50%)
        double skillScore = calculateSkillScore(candidate, resume, job, feedback);
        totalScore += skillScore * SKILL_WEIGHT;

        // 2. Experience Match Score (25%)
        double experienceScore = calculateExperienceScore(candidate, job, feedback);
        totalScore += experienceScore * EXPERIENCE_WEIGHT;

        // 3. Education Score (10%)
        double educationScore = calculateEducationScore(candidate, resume, feedback);
        totalScore += educationScore * EDUCATION_WEIGHT;

        // 4. Resume Quality Score (10%)
        double resumeQualityScore = calculateResumeQualityScore(resume, feedback);
        totalScore += resumeQualityScore * RESUME_QUALITY_WEIGHT;

        // 5. Recency Bonus (5%)
        double recencyScore = calculateRecencyScore(application, feedback);
        totalScore += recencyScore * RECENCY_WEIGHT;

        // Convert to percentage (0-100)
        double finalScore = totalScore * 100;

        // Round to 2 decimal places
        BigDecimal score = BigDecimal.valueOf(finalScore)
                .setScale(2, RoundingMode.HALF_UP);

        // Update application with score and feedback
        application.setAiScore(score);
        application.setAiFeedback(feedback.toString());
        application.setUpdatedAt(LocalDateTime.now());
        applicationRepository.save(application);

        log.info("AI Score calculated for application {}: {}", application.getId(), score);

        return score;
    }

    /**
     * Calculate skill match score
     */
    private double calculateSkillScore(Candidate candidate, Resume resume, Job job, StringBuilder feedback) {
        String candidateSkills = candidate.getSkills();

        // Also consider skills from resume
        if (resume != null && resume.getExtractedSkills() != null) {
            if (candidateSkills == null || candidateSkills.isEmpty()) {
                candidateSkills = resume.getExtractedSkills();
            } else {
                candidateSkills = candidateSkills + ", " + resume.getExtractedSkills();
            }
        }

        String requiredSkills = job.getRequiredSkills();

        Map<String, Object> skillMatch = skillMatcherService.getDetailedSkillMatch(
                candidateSkills, requiredSkills);

        double score = (Double) skillMatch.get("matchScore");
        int matched = (Integer) skillMatch.get("totalMatched");
        int total = (Integer) skillMatch.get("totalRequired");

        @SuppressWarnings("unchecked")
        List<String> missingSkills = (List<String>) skillMatch.get("missingSkills");

        feedback.append(String.format("Skills: %d/%d matched (%.0f%%). ", matched, total, score * 100));

        if (!missingSkills.isEmpty() && missingSkills.size() <= 3) {
            feedback.append("Missing: ").append(String.join(", ", missingSkills)).append(". ");
        } else if (missingSkills.size() > 3) {
            feedback.append("Missing ").append(missingSkills.size()).append(" skills. ");
        }

        return score;
    }

    /**
     * Calculate experience match score
     */
    private double calculateExperienceScore(Candidate candidate, Job job, StringBuilder feedback) {
        BigDecimal candidateExp = candidate.getTotalExperience();
        Integer minExp = job.getExperienceMin();
        Integer maxExp = job.getExperienceMax();

        if (candidateExp == null) {
            feedback.append("Experience: Not specified. ");
            return 0.5; // Neutral score if not specified
        }

        double years = candidateExp.doubleValue();

        // No requirements specified
        if (minExp == null && maxExp == null) {
            feedback.append(String.format("Experience: %.1f years (no requirement). ", years));
            return 1.0;
        }

        int min = minExp != null ? minExp : 0;
        int max = maxExp != null ? maxExp : 50;

        double score;

        if (years >= min && years <= max) {
            // Perfect fit
            score = 1.0;
            feedback.append(String.format("Experience: %.1f years (ideal range %d-%d). ", years, min, max));
        } else if (years < min) {
            // Under-qualified
            double deficit = min - years;
            score = Math.max(0, 1 - (deficit * 0.2));
            feedback.append(String.format("Experience: %.1f years (below required %d). ", years, min));
        } else {
            // Over-qualified (slight penalty)
            score = 0.85;
            feedback.append(String.format("Experience: %.1f years (above range, may be overqualified). ", years));
        }

        return score;
    }

    /**
     * Calculate education score
     */
    private double calculateEducationScore(Candidate candidate, Resume resume, StringBuilder feedback) {
        String education = candidate.getEducation();

        if (education == null || education.isEmpty()) {
            if (resume != null && resume.getExtractedEducation() != null) {
                education = resume.getExtractedEducation();
            }
        }

        if (education == null || education.isEmpty()) {
            feedback.append("Education: Not specified. ");
            return 0.5;
        }

        String lowerEducation = education.toLowerCase();
        double score = 0.5;

        // Score based on education level
        if (lowerEducation.contains("phd") || lowerEducation.contains("doctorate")) {
            score = 1.0;
            feedback.append("Education: PhD/Doctorate. ");
        } else if (lowerEducation.contains("master") || lowerEducation.contains("m.tech") ||
                lowerEducation.contains("mba") || lowerEducation.contains("mca")) {
            score = 0.9;
            feedback.append("Education: Master's degree. ");
        } else if (lowerEducation.contains("bachelor") || lowerEducation.contains("b.tech") ||
                lowerEducation.contains("b.e") || lowerEducation.contains("bca")) {
            score = 0.8;
            feedback.append("Education: Bachelor's degree. ");
        } else if (lowerEducation.contains("diploma")) {
            score = 0.6;
            feedback.append("Education: Diploma. ");
        } else {
            feedback.append("Education: Found. ");
        }

        return score;
    }

    /**
     * Calculate resume quality score
     */
    private double calculateResumeQualityScore(Resume resume, StringBuilder feedback) {
        if (resume == null) {
            feedback.append("Resume: Not uploaded. ");
            return 0.0;
        }

        double score = 0.5; // Base score for having a resume

        // Check if resume was parsed
        if (resume.getParsedText() != null && !resume.getParsedText().isEmpty()) {
            int textLength = resume.getParsedText().length();

            if (textLength > 2000) {
                score = 1.0; // Detailed resume
            } else if (textLength > 1000) {
                score = 0.8;
            } else if (textLength > 500) {
                score = 0.6;
            }
        }

        // Bonus for extracted skills
        if (resume.getExtractedSkills() != null && !resume.getExtractedSkills().isEmpty()) {
            int skillCount = resume.getExtractedSkills().split(",").length;
            if (skillCount > 10) {
                score = Math.min(1.0, score + 0.1);
            }
        }

        feedback.append(String.format("Resume quality: %.0f%%. ", score * 100));

        return score;
    }

    /**
     * Calculate recency score (newer applications get slight boost)
     */
    private double calculateRecencyScore(Application application, StringBuilder feedback) {
        if (application.getAppliedAt() == null) {
            return 0.5;
        }

        long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(
                application.getAppliedAt().toLocalDate(),
                java.time.LocalDate.now()
        );

        double score;
        if (daysAgo <= 1) {
            score = 1.0;
        } else if (daysAgo <= 7) {
            score = 0.9;
        } else if (daysAgo <= 30) {
            score = 0.7;
        } else {
            score = 0.5;
        }

        return score;
    }

    /**
     * Score all applications for a job
     */
    @Transactional
    public List<ApplicationResponse> scoreAllApplicationsForJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        List<Application> applications = applicationRepository.findByJobId(jobId);

        log.info("Scoring {} applications for job: {}", applications.size(), jobId);

        for (Application application : applications) {
            if (application.getAiScore() == null) {
                calculateScore(application);
            }
        }

        // Return sorted by score
        return applications.stream()
                .sorted((a, b) -> {
                    BigDecimal scoreA = a.getAiScore() != null ? a.getAiScore() : BigDecimal.ZERO;
                    BigDecimal scoreB = b.getAiScore() != null ? b.getAiScore() : BigDecimal.ZERO;
                    return scoreB.compareTo(scoreA);
                })
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get top candidates for a job
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getTopCandidates(Long jobId, int limit) {
        List<Application> applications = applicationRepository.findByJobId(jobId);

        return applications.stream()
                .filter(a -> a.getAiScore() != null)
                .sorted((a, b) -> b.getAiScore().compareTo(a.getAiScore()))
                .limit(limit)
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get candidates above threshold
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> getCandidatesAboveThreshold(Long jobId, double threshold) {
        BigDecimal thresholdScore = BigDecimal.valueOf(threshold);

        List<Application> applications = applicationRepository.findTopCandidatesByScore(
                jobId, thresholdScore);

        return applications.stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get AI score statistics for a job
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getScoreStatistics(Long jobId) {
        List<Application> applications = applicationRepository.findByJobId(jobId);

        List<BigDecimal> scores = applications.stream()
                .filter(a -> a.getAiScore() != null)
                .map(Application::getAiScore)
                .collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();

        if (scores.isEmpty()) {
            stats.put("totalApplications", applications.size());
            stats.put("scoredApplications", 0);
            stats.put("averageScore", 0);
            stats.put("highestScore", 0);
            stats.put("lowestScore", 0);
            return stats;
        }

        double average = scores.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0);

        double highest = scores.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .max()
                .orElse(0);

        double lowest = scores.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .min()
                .orElse(0);

        // Score distribution
        long above80 = scores.stream().filter(s -> s.doubleValue() >= 80).count();
        long between60And80 = scores.stream()
                .filter(s -> s.doubleValue() >= 60 && s.doubleValue() < 80).count();
        long between40And60 = scores.stream()
                .filter(s -> s.doubleValue() >= 40 && s.doubleValue() < 60).count();
        long below40 = scores.stream().filter(s -> s.doubleValue() < 40).count();

        stats.put("totalApplications", applications.size());
        stats.put("scoredApplications", scores.size());
        stats.put("averageScore", BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP));
        stats.put("highestScore", BigDecimal.valueOf(highest).setScale(2, RoundingMode.HALF_UP));
        stats.put("lowestScore", BigDecimal.valueOf(lowest).setScale(2, RoundingMode.HALF_UP));
        stats.put("excellentMatch", above80);      // >= 80%
        stats.put("goodMatch", between60And80);     // 60-80%
        stats.put("averageMatch", between40And60);  // 40-60%
        stats.put("poorMatch", below40);            // < 40%

        return stats;
    }

    /**
     * Re-score all pending applications for a job
     */
    @Transactional
    public int rescoreApplications(Long jobId) {
        List<Application> applications = applicationRepository.findApplicationsNeedingScoringByJob(jobId);

        for (Application application : applications) {
            calculateScore(application);
        }

        log.info("Re-scored {} applications for job {}", applications.size(), jobId);

        return applications.size();
    }

    /**
     * Get match explanation for an application
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMatchExplanation(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        Job job = application.getJob();
        Candidate candidate = application.getCandidate();
        Resume resume = application.getResume() != null ?
                application.getResume() : candidate.getPrimaryResume();

        Map<String, Object> explanation = new HashMap<>();

        // Overall score
        explanation.put("overallScore", application.getAiScore());
        explanation.put("aiFeedback", application.getAiFeedback());

        // Skill breakdown
        String candidateSkills = candidate.getSkills();
        if (resume != null && resume.getExtractedSkills() != null) {
            candidateSkills = candidateSkills != null ?
                    candidateSkills + ", " + resume.getExtractedSkills() :
                    resume.getExtractedSkills();
        }

        Map<String, Object> skillMatch = skillMatcherService.getDetailedSkillMatch(
                candidateSkills, job.getRequiredSkills());

        explanation.put("skillAnalysis", skillMatch);

        // Experience analysis
        Map<String, Object> expAnalysis = new HashMap<>();
        expAnalysis.put("candidateExperience", candidate.getTotalExperience());
        expAnalysis.put("requiredMin", job.getExperienceMin());
        expAnalysis.put("requiredMax", job.getExperienceMax());
        explanation.put("experienceAnalysis", expAnalysis);

        // Recommendations
        List<String> recommendations = generateRecommendations(application, skillMatch);
        explanation.put("recommendations", recommendations);

        return explanation;
    }

    /**
     * Generate improvement recommendations
     */
    @SuppressWarnings("unchecked")
    private List<String> generateRecommendations(Application application, Map<String, Object> skillMatch) {
        List<String> recommendations = new ArrayList<>();

        List<String> missingSkills = (List<String>) skillMatch.get("missingSkills");

        if (missingSkills != null && !missingSkills.isEmpty()) {
            if (missingSkills.size() <= 3) {
                recommendations.add("Consider gaining experience in: " + String.join(", ", missingSkills));
            } else {
                recommendations.add("Candidate is missing " + missingSkills.size() +
                        " required skills. May need additional training.");
            }
        }

        BigDecimal score = application.getAiScore();
        if (score != null) {
            if (score.doubleValue() >= 80) {
                recommendations.add("Strong candidate - recommend for interview");
            } else if (score.doubleValue() >= 60) {
                recommendations.add("Good candidate - consider for technical screening");
            } else if (score.doubleValue() >= 40) {
                recommendations.add("Average match - review manually");
            } else {
                recommendations.add("Low match - may not be suitable for this role");
            }
        }

        return recommendations;
    }
}