package com.smarthiring.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Getter
@Slf4j
public class FileStorageConfig {

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${file.upload.resume-dir:./uploads/resumes}")
    private String resumeUploadDir;

    @Value("${file.upload.profile-dir:./uploads/profiles}")
    private String profileUploadDir;

    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize; // 10MB default

    private Path resumePath;
    private Path profilePath;

    @PostConstruct
    public void init() {
        try {
            // Create resume upload directory
            resumePath = Paths.get(resumeUploadDir).toAbsolutePath().normalize();
            Files.createDirectories(resumePath);
            log.info("Resume upload directory created: {}", resumePath);

            // Create profile picture upload directory
            profilePath = Paths.get(profileUploadDir).toAbsolutePath().normalize();
            Files.createDirectories(profilePath);
            log.info("Profile upload directory created: {}", profilePath);

        } catch (IOException e) {
            log.error("Could not create upload directories", e);
            throw new RuntimeException("Could not create upload directories", e);
        }
    }

    public Path getResumePath() {
        return resumePath;
    }

    public Path getProfilePath() {
        return profilePath;
    }
}