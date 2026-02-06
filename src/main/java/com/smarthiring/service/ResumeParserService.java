package com.smarthiring.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ResumeParserService {

    private final Tika tika = new Tika();

    // Common technical skills to extract
    private static final Set<String> KNOWN_SKILLS = new HashSet<>(Arrays.asList(
            // Programming Languages
            "java", "python", "javascript", "typescript", "c++", "c#", "go", "golang",
            "ruby", "php", "swift", "kotlin", "scala", "rust", "r", "matlab",

            // Frontend
            "react", "reactjs", "react.js", "angular", "angularjs", "vue", "vuejs", "vue.js",
            "html", "html5", "css", "css3", "sass", "scss", "less", "bootstrap", "tailwind",
            "jquery", "next.js", "nextjs", "nuxt", "gatsby", "webpack", "redux",

            // Backend
            "spring", "spring boot", "springboot", "hibernate", "jpa", "node.js", "nodejs",
            "express", "expressjs", "django", "flask", "fastapi", "rails", "laravel",
            "asp.net", ".net", "dotnet", "microservices", "rest", "restful", "graphql",

            // Databases
            "sql", "mysql", "postgresql", "postgres", "oracle", "mongodb", "redis",
            "elasticsearch", "cassandra", "dynamodb", "sqlite", "mariadb", "firebase",

            // Cloud & DevOps
            "aws", "amazon web services", "azure", "gcp", "google cloud", "docker",
            "kubernetes", "k8s", "jenkins", "ci/cd", "terraform", "ansible", "linux",
            "unix", "bash", "shell scripting", "nginx", "apache",

            // Tools & Others
            "git", "github", "gitlab", "bitbucket", "jira", "confluence", "maven", "gradle",
            "npm", "yarn", "postman", "swagger", "junit", "selenium", "testng", "mockito",

            // Data & ML
            "machine learning", "ml", "deep learning", "ai", "artificial intelligence",
            "tensorflow", "pytorch", "keras", "pandas", "numpy", "scikit-learn",
            "data science", "data analysis", "big data", "hadoop", "spark", "kafka",

            // Mobile
            "android", "ios", "react native", "flutter", "xamarin", "ionic",

            // Others
            "agile", "scrum", "kanban", "api", "json", "xml", "oauth", "jwt"
    ));

    // Education keywords
    private static final Set<String> EDUCATION_KEYWORDS = new HashSet<>(Arrays.asList(
            "bachelor", "master", "phd", "b.tech", "m.tech", "b.e", "m.e", "bca", "mca",
            "b.sc", "m.sc", "bba", "mba", "diploma", "degree", "university", "college",
            "institute", "school", "education", "graduated", "graduation"
    ));

    /**
     * Parse resume and extract text
     */
    public String parseResume(MultipartFile file) {
        try (InputStream stream = file.getInputStream()) {
            String text = tika.parseToString(stream);
            log.info("Resume parsed successfully. Extracted {} characters", text.length());
            return text;
        } catch (IOException | TikaException e) {
            log.error("Error parsing resume: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Parse resume from file path
     */
    public String parseResume(java.nio.file.Path filePath) {
        try (InputStream stream = java.nio.file.Files.newInputStream(filePath)) {
            String text = tika.parseToString(stream);
            log.info("Resume parsed successfully from path. Extracted {} characters", text.length());
            return text;
        } catch (IOException | TikaException e) {
            log.error("Error parsing resume from path: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Extract skills from resume text
     */
    public String extractSkills(String resumeText) {
        if (resumeText == null || resumeText.isEmpty()) {
            return "";
        }

        String lowerText = resumeText.toLowerCase();
        Set<String> foundSkills = new LinkedHashSet<>();

        for (String skill : KNOWN_SKILLS) {
            // Check if skill exists in text (with word boundaries)
            String pattern = "\\b" + Pattern.quote(skill) + "\\b";
            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(lowerText).find()) {
                // Capitalize first letter for display
                foundSkills.add(capitalizeSkill(skill));
            }
        }

        String result = String.join(", ", foundSkills);
        log.info("Extracted {} skills from resume", foundSkills.size());

        return result;
    }

    /**
     * Extract email from resume
     */
    public String extractEmail(String resumeText) {
        if (resumeText == null) return null;

        Pattern pattern = Pattern.compile(
                "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(resumeText);

        if (matcher.find()) {
            return matcher.group().toLowerCase();
        }
        return null;
    }

    /**
     * Extract phone number from resume
     */
    public String extractPhone(String resumeText) {
        if (resumeText == null) return null;

        // Pattern for Indian phone numbers
        Pattern pattern = Pattern.compile(
                "(\\+91[\\s-]?)?[6-9]\\d{9}|" +
                        "(\\+1[\\s-]?)?\\(?\\d{3}\\)?[\\s-]?\\d{3}[\\s-]?\\d{4}"
        );
        Matcher matcher = pattern.matcher(resumeText);

        if (matcher.find()) {
            return matcher.group().replaceAll("[\\s-()]", "");
        }
        return null;
    }

    /**
     * Extract years of experience
     */
    public Double extractExperience(String resumeText) {
        if (resumeText == null) return null;

        // Pattern to find years of experience
        Pattern pattern = Pattern.compile(
                "(\\d+(?:\\.\\d+)?)[+]?\\s*(?:years?|yrs?|\\+)\\s*(?:of)?\\s*(?:experience|exp)?",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(resumeText);

        Double maxExperience = null;
        while (matcher.find()) {
            try {
                Double years = Double.parseDouble(matcher.group(1));
                if (maxExperience == null || years > maxExperience) {
                    maxExperience = years;
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        return maxExperience;
    }

    /**
     * Extract education information
     */
    public String extractEducation(String resumeText) {
        if (resumeText == null || resumeText.isEmpty()) {
            return "";
        }

        String lowerText = resumeText.toLowerCase();
        List<String> educationFound = new ArrayList<>();

        // Split text into lines and find education-related lines
        String[] lines = resumeText.split("\\r?\\n");

        for (String line : lines) {
            String lowerLine = line.toLowerCase();
            for (String keyword : EDUCATION_KEYWORDS) {
                if (lowerLine.contains(keyword) && line.trim().length() > 10) {
                    educationFound.add(line.trim());
                    break;
                }
            }
        }

        // Take first 3 education entries
        return educationFound.stream()
                .limit(3)
                .collect(Collectors.joining("; "));
    }

    /**
     * Extract name (usually at the beginning)
     */
    public String extractName(String resumeText) {
        if (resumeText == null || resumeText.isEmpty()) {
            return null;
        }

        // Get first few lines
        String[] lines = resumeText.split("\\r?\\n");

        for (String line : lines) {
            line = line.trim();

            // Skip empty lines
            if (line.isEmpty()) continue;

            // Skip lines that look like emails, phones, or URLs
            if (line.contains("@") || line.matches(".*\\d{10}.*") || line.contains("http")) {
                continue;
            }

            // Name is usually 2-4 words, all letters
            if (line.matches("^[A-Za-z]+(?:\\s+[A-Za-z]+){1,3}$")) {
                return line;
            }
        }

        return null;
    }

    /**
     * Capitalize skill name properly
     */
    private String capitalizeSkill(String skill) {
        // Skills that should remain uppercase
        Set<String> upperCaseSkills = new HashSet<>(Arrays.asList(
                "sql", "html", "css", "php", "api", "aws", "gcp", "ci/cd",
                "jwt", "xml", "json", "npm", "ai", "ml", "jpa", "mvc"
        ));

        if (upperCaseSkills.contains(skill.toLowerCase())) {
            return skill.toUpperCase();
        }

        // Capitalize first letter
        if (skill.length() <= 1) {
            return skill.toUpperCase();
        }

        return Character.toUpperCase(skill.charAt(0)) + skill.substring(1);
    }

    /**
     * Get all parsed information as a map
     */
    public Map<String, Object> parseResumeComplete(MultipartFile file) {
        String text = parseResume(file);

        Map<String, Object> result = new HashMap<>();
        result.put("parsedText", text);
        result.put("skills", extractSkills(text));
        result.put("email", extractEmail(text));
        result.put("phone", extractPhone(text));
        result.put("experience", extractExperience(text));
        result.put("education", extractEducation(text));
        result.put("name", extractName(text));

        return result;
    }
}