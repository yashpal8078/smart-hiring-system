package com.smarthiring.service;

import com.smarthiring.config.FileStorageConfig;
import com.smarthiring.exception.BadRequestException;
import com.smarthiring.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileStorageConfig fileStorageConfig;

    // Allowed file extensions
    private static final List<String> ALLOWED_RESUME_EXTENSIONS = Arrays.asList("pdf", "doc", "docx");
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");

    /**
     * Store resume file
     */
    public String storeResumeFile(MultipartFile file) {
        return storeFile(file, fileStorageConfig.getResumePath(), ALLOWED_RESUME_EXTENSIONS);
    }

    /**
     * Store profile picture
     */
    public String storeProfilePicture(MultipartFile file) {
        return storeFile(file, fileStorageConfig.getProfilePath(), ALLOWED_IMAGE_EXTENSIONS);
    }

    /**
     * Generic file storage
     */
    private String storeFile(MultipartFile file, Path targetPath, List<String> allowedExtensions) {
        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("Cannot upload empty file");
        }

        // Get original filename and clean it
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Check for invalid characters
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Invalid file path: " + originalFilename);
        }

        // Get file extension
        String extension = getFileExtension(originalFilename);

        // Validate extension
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new BadRequestException("File type not allowed. Allowed types: " + allowedExtensions);
        }

        // Check file size
        if (file.getSize() > fileStorageConfig.getMaxFileSize()) {
            throw new BadRequestException("File size exceeds maximum limit of " +
                    (fileStorageConfig.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        // Generate unique filename
        String newFilename = UUID.randomUUID().toString() + "." + extension;

        try {
            // Copy file to target location
            Path targetLocation = targetPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored successfully: {}", newFilename);

            return newFilename;

        } catch (IOException e) {
            log.error("Could not store file: {}", originalFilename, e);
            throw new BadRequestException("Could not store file: " + originalFilename);
        }
    }

    /**
     * Load resume file as Resource
     */
    public Resource loadResumeAsResource(String filename) {
        return loadFileAsResource(filename, fileStorageConfig.getResumePath());
    }

    /**
     * Load profile picture as Resource
     */
    public Resource loadProfilePictureAsResource(String filename) {
        return loadFileAsResource(filename, fileStorageConfig.getProfilePath());
    }

    /**
     * Generic file loading
     */
    private Resource loadFileAsResource(String filename, Path basePath) {
        try {
            Path filePath = basePath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File", "name", filename);
            }

        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File", "name", filename);
        }
    }

    /**
     * Delete resume file
     */
    public boolean deleteResumeFile(String filename) {
        return deleteFile(filename, fileStorageConfig.getResumePath());
    }

    /**
     * Delete profile picture
     */
    public boolean deleteProfilePicture(String filename) {
        return deleteFile(filename, fileStorageConfig.getProfilePath());
    }

    /**
     * Generic file deletion
     */
    private boolean deleteFile(String filename, Path basePath) {
        try {
            Path filePath = basePath.resolve(filename).normalize();
            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                log.info("File deleted: {}", filename);
            }

            return deleted;

        } catch (IOException e) {
            log.error("Could not delete file: {}", filename, e);
            return false;
        }
    }

    /**
     * Get file extension
     */
    public String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Get content type for file
     */
    public String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();

        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }

    /**
     * Check if file exists
     */
    public boolean resumeFileExists(String filename) {
        Path filePath = fileStorageConfig.getResumePath().resolve(filename);
        return Files.exists(filePath);
    }
}