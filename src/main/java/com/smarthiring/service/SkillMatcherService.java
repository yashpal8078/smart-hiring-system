package com.smarthiring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SkillMatcherService {

    // Skill synonyms and related terms
    private static final Map<String, Set<String>> SKILL_SYNONYMS = new HashMap<>();

    static {
        // JavaScript variants
        SKILL_SYNONYMS.put("javascript", Set.of("js", "es6", "es2015", "ecmascript"));
        SKILL_SYNONYMS.put("typescript", Set.of("ts"));

        // Java ecosystem
        SKILL_SYNONYMS.put("java", Set.of("j2ee", "jee", "core java", "java8", "java11", "java17"));
        SKILL_SYNONYMS.put("spring boot", Set.of("springboot", "spring-boot", "spring framework", "spring"));
        SKILL_SYNONYMS.put("hibernate", Set.of("jpa", "orm"));

        // Frontend frameworks
        SKILL_SYNONYMS.put("react", Set.of("reactjs", "react.js", "react js"));
        SKILL_SYNONYMS.put("angular", Set.of("angularjs", "angular.js", "angular2", "angular4"));
        SKILL_SYNONYMS.put("vue", Set.of("vuejs", "vue.js", "vue js"));
        SKILL_SYNONYMS.put("next.js", Set.of("nextjs", "next"));

        // Backend
        SKILL_SYNONYMS.put("node.js", Set.of("nodejs", "node", "node js"));
        SKILL_SYNONYMS.put("express", Set.of("expressjs", "express.js"));
        SKILL_SYNONYMS.put("python", Set.of("python3", "py"));
        SKILL_SYNONYMS.put("django", Set.of("django rest", "drf"));

        // Databases
        SKILL_SYNONYMS.put("mysql", Set.of("my sql", "mariadb"));
        SKILL_SYNONYMS.put("postgresql", Set.of("postgres", "psql", "pgsql"));
        SKILL_SYNONYMS.put("mongodb", Set.of("mongo", "nosql"));
        SKILL_SYNONYMS.put("sql", Set.of("structured query language", "rdbms"));

        // Cloud & DevOps
        SKILL_SYNONYMS.put("aws", Set.of("amazon web services", "amazon aws", "ec2", "s3", "lambda"));
        SKILL_SYNONYMS.put("azure", Set.of("microsoft azure", "ms azure"));
        SKILL_SYNONYMS.put("gcp", Set.of("google cloud", "google cloud platform"));
        SKILL_SYNONYMS.put("docker", Set.of("containerization", "containers"));
        SKILL_SYNONYMS.put("kubernetes", Set.of("k8s", "container orchestration"));
        SKILL_SYNONYMS.put("ci/cd", Set.of("cicd", "continuous integration", "continuous deployment", "devops"));

        // Others
        SKILL_SYNONYMS.put("rest api", Set.of("restful", "rest", "restful api", "web services"));
        SKILL_SYNONYMS.put("microservices", Set.of("micro services", "microservice architecture"));
        SKILL_SYNONYMS.put("git", Set.of("github", "gitlab", "bitbucket", "version control"));
        SKILL_SYNONYMS.put("agile", Set.of("scrum", "kanban", "agile methodology"));
        SKILL_SYNONYMS.put("machine learning", Set.of("ml", "deep learning", "ai", "artificial intelligence"));
    }

    // Skill categories with weights
    private static final Map<String, Double> SKILL_CATEGORY_WEIGHTS = new HashMap<>();

    static {
        SKILL_CATEGORY_WEIGHTS.put("programming_language", 1.0);
        SKILL_CATEGORY_WEIGHTS.put("framework", 0.9);
        SKILL_CATEGORY_WEIGHTS.put("database", 0.8);
        SKILL_CATEGORY_WEIGHTS.put("cloud", 0.7);
        SKILL_CATEGORY_WEIGHTS.put("tool", 0.6);
        SKILL_CATEGORY_WEIGHTS.put("soft_skill", 0.5);
    }

    /**
     * Calculate skill match score between candidate skills and required skills
     * Returns a score between 0.0 and 1.0
     */
    public double calculateSkillMatchScore(String candidateSkills, String requiredSkills) {
        if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
            return 1.0; // No requirements = full match
        }

        if (candidateSkills == null || candidateSkills.trim().isEmpty()) {
            return 0.0; // No skills = no match
        }

        Set<String> candidateSkillSet = parseSkills(candidateSkills);
        Set<String> requiredSkillSet = parseSkills(requiredSkills);

        if (requiredSkillSet.isEmpty()) {
            return 1.0;
        }

        int matchedSkills = 0;
        int totalRequired = requiredSkillSet.size();

        for (String required : requiredSkillSet) {
            if (hasSkillMatch(candidateSkillSet, required)) {
                matchedSkills++;
            }
        }

        double score = (double) matchedSkills / totalRequired;

        log.debug("Skill match: {}/{} = {}", matchedSkills, totalRequired, score);

        return score;
    }

    /**
     * Get detailed skill match analysis
     */
    public Map<String, Object> getDetailedSkillMatch(String candidateSkills, String requiredSkills) {
        Map<String, Object> result = new HashMap<>();

        Set<String> candidateSkillSet = parseSkills(candidateSkills);
        Set<String> requiredSkillSet = parseSkills(requiredSkills);

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        List<String> extraSkills = new ArrayList<>();

        // Find matched and missing skills
        for (String required : requiredSkillSet) {
            if (hasSkillMatch(candidateSkillSet, required)) {
                matchedSkills.add(required);
            } else {
                missingSkills.add(required);
            }
        }

        // Find extra skills (candidate has but not required)
        for (String candidate : candidateSkillSet) {
            boolean isRequired = false;
            for (String required : requiredSkillSet) {
                if (hasSkillMatch(Set.of(candidate), required)) {
                    isRequired = true;
                    break;
                }
            }
            if (!isRequired) {
                extraSkills.add(candidate);
            }
        }

        double matchScore = requiredSkillSet.isEmpty() ? 1.0 :
                (double) matchedSkills.size() / requiredSkillSet.size();

        result.put("matchScore", matchScore);
        result.put("matchedSkills", matchedSkills);
        result.put("missingSkills", missingSkills);
        result.put("extraSkills", extraSkills);
        result.put("totalRequired", requiredSkillSet.size());
        result.put("totalMatched", matchedSkills.size());

        return result;
    }

    /**
     * Check if candidate has a matching skill (including synonyms)
     */
    private boolean hasSkillMatch(Set<String> candidateSkills, String requiredSkill) {
        String normalizedRequired = normalizeSkill(requiredSkill);

        // Direct match
        for (String candidateSkill : candidateSkills) {
            String normalizedCandidate = normalizeSkill(candidateSkill);

            // Exact match
            if (normalizedCandidate.equals(normalizedRequired)) {
                return true;
            }

            // Contains match
            if (normalizedCandidate.contains(normalizedRequired) ||
                    normalizedRequired.contains(normalizedCandidate)) {
                return true;
            }
        }

        // Check synonyms
        Set<String> synonyms = SKILL_SYNONYMS.get(normalizedRequired);
        if (synonyms != null) {
            for (String candidateSkill : candidateSkills) {
                String normalizedCandidate = normalizeSkill(candidateSkill);
                if (synonyms.contains(normalizedCandidate)) {
                    return true;
                }
            }
        }

        // Reverse synonym check
        for (Map.Entry<String, Set<String>> entry : SKILL_SYNONYMS.entrySet()) {
            if (entry.getValue().contains(normalizedRequired)) {
                String mainSkill = entry.getKey();
                for (String candidateSkill : candidateSkills) {
                    if (normalizeSkill(candidateSkill).equals(mainSkill)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Parse comma-separated skills into a set
     */
    public Set<String> parseSkills(String skills) {
        if (skills == null || skills.trim().isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Normalize skill name for comparison
     */
    private String normalizeSkill(String skill) {
        return skill.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9+#.]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Calculate skill similarity using Jaccard index
     */
    public double calculateJaccardSimilarity(String skills1, String skills2) {
        Set<String> set1 = parseSkills(skills1);
        Set<String> set2 = parseSkills(skills2);

        if (set1.isEmpty() && set2.isEmpty()) {
            return 1.0;
        }

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
}