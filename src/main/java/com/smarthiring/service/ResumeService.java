package com.smarthiring.service;

import com.smarthiring.config.FileStorageConfig;
import com.smarthiring.dto.response.ResumeResponse;
import com.smarthiring.entity.Candidate;
import com.smarthiring.entity.Resume;
import com.smarthiring.exception.BadRequestException;
import com.smarthiring.exception.ResourceNotFoundException;
import com.smarthiring.mapper.CandidateMapper;
import com.smarthiring.repository.CandidateRepository;
import com.smarthiring.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final CandidateRepository candidateRepository;
    private final FileStorageService fileStorageService;
    private final ResumeParserService resumeParserService;
    private final FileStorageConfig fileStorageConfig;
    private final CandidateMapper candidateMapper;

    /**
     * Upload resume for a candidate
     */
    @Transactional
    public ResumeResponse uploadResume(Long userId, MultipartFile file, boolean makePrimary) {
        log.info("Uploading resume for user: {}", userId);

        // Get candidate
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "userId", userId));

        // Validate file
        validateResumeFile(file);

        // Store file
        String storedFileName = fileStorageService.storeResumeFile(file);
        String originalFileName = file.getOriginalFilename();
        String fileType = fileStorageService.getFileExtension(originalFileName);
        long fileSize = file.getSize();

        // Parse resume
        Map<String, Object> parsedData = resumeParserService.parseResumeComplete(file);

        // If makePrimary, reset all other resumes
        if (makePrimary) {
            resumeRepository.resetPrimaryResume(candidate.getId());
        }

        // Check if this is the first resume (make it primary by default)
        boolean isPrimary = makePrimary || !resumeRepository.existsByCandidateId(candidate.getId());

        // Create resume entity
        Resume resume = Resume.builder()
                .candidate(candidate)
                .fileName(storedFileName)
                .originalFileName(originalFileName)
                .filePath(fileStorageConfig.getResumePath().resolve(storedFileName).toString())
                .fileType(fileType)
                .fileSize(fileSize)
                .parsedText((String) parsedData.get("parsedText"))
                .extractedSkills((String) parsedData.get("skills"))
                .extractedExperience(parsedData.get("experience") != null ?
                        parsedData.get("experience").toString() : null)
                .extractedEducation((String) parsedData.get("education"))
                .isPrimary(isPrimary)
                .build();

        Resume savedResume = resumeRepository.save(resume);

        // Update candidate skills if empty
        if (candidate.getSkills() == null || candidate.getSkills().isEmpty()) {
            candidate.setSkills((String) parsedData.get("skills"));
            candidateRepository.save(candidate);
        }

        log.info("Resume uploaded successfully: {} for candidate: {}", savedResume.getId(), candidate.getId());

        return candidateMapper.toResumeResponse(savedResume);
    }

    /**
     * Get resume by ID
     */
    @Transactional(readOnly = true)
    public ResumeResponse getResumeById(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        return candidateMapper.toResumeResponse(resume);
    }

    /**
     * Get all resumes for a candidate
     */
    @Transactional(readOnly = true)
    public List<ResumeResponse> getResumesByCandidate(Long userId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "userId", userId));

        return resumeRepository.findByCandidateId(candidate.getId()).stream()
                .map(candidateMapper::toResumeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Download resume as Resource
     */
    @Transactional(readOnly = true)
    public Resource downloadResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        return fileStorageService.loadResumeAsResource(resume.getFileName());
    }

    /**
     * Get resume content type
     */
    @Transactional(readOnly = true)
    public String getResumeContentType(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        return fileStorageService.getContentType(resume.getFileName());
    }

    /**
     * Get resume original filename
     */
    @Transactional(readOnly = true)
    public String getResumeOriginalFilename(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        return resume.getOriginalFileName();
    }

    /**
     * Set resume as primary
     */
    @Transactional
    public ResumeResponse setPrimaryResume(Long userId, Long resumeId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "userId", userId));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        // Verify resume belongs to candidate
        if (!resume.getCandidate().getId().equals(candidate.getId())) {
            throw new BadRequestException("Resume does not belong to you");
        }

        // Reset all resumes to non-primary
        resumeRepository.resetPrimaryResume(candidate.getId());

        // Set this resume as primary
        resume.setIsPrimary(true);
        Resume savedResume = resumeRepository.save(resume);

        log.info("Resume {} set as primary for candidate {}", resumeId, candidate.getId());

        return candidateMapper.toResumeResponse(savedResume);
    }

    /**
     * Delete resume
     */
    @Transactional
    public void deleteResume(Long userId, Long resumeId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "userId", userId));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        // Verify resume belongs to candidate
        if (!resume.getCandidate().getId().equals(candidate.getId())) {
            throw new BadRequestException("Resume does not belong to you");
        }

        // Delete file from storage
        fileStorageService.deleteResumeFile(resume.getFileName());

        // Delete from database
        resumeRepository.delete(resume);

        // If deleted resume was primary, make another one primary
        if (resume.getIsPrimary()) {
            List<Resume> remainingResumes = resumeRepository.findByCandidateId(candidate.getId());
            if (!remainingResumes.isEmpty()) {
                remainingResumes.get(0).setIsPrimary(true);
                resumeRepository.save(remainingResumes.get(0));
            }
        }

        log.info("Resume {} deleted for candidate {}", resumeId, candidate.getId());
    }

    /**
     * Re-parse resume (extract skills again)
     */
    @Transactional
    public ResumeResponse reparseResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        // Parse resume file again
        Path filePath = fileStorageConfig.getResumePath().resolve(resume.getFileName());
        String parsedText = resumeParserService.parseResume(filePath);

        // Extract data
        String skills = resumeParserService.extractSkills(parsedText);
        Double experience = resumeParserService.extractExperience(parsedText);
        String education = resumeParserService.extractEducation(parsedText);

        // Update resume
        resume.setParsedText(parsedText);
        resume.setExtractedSkills(skills);
        resume.setExtractedExperience(experience != null ? experience.toString() : null);
        resume.setExtractedEducation(education);

        Resume savedResume = resumeRepository.save(resume);

        log.info("Resume {} re-parsed successfully", resumeId);

        return candidateMapper.toResumeResponse(savedResume);
    }

    /**
     * Get primary resume for candidate
     */
    @Transactional(readOnly = true)
    public ResumeResponse getPrimaryResume(Long userId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "userId", userId));

        Resume primaryResume = resumeRepository.findByCandidateIdAndIsPrimaryTrue(candidate.getId())
                .orElse(null);

        if (primaryResume == null) {
            // Return first resume if no primary set
            List<Resume> resumes = resumeRepository.findByCandidateId(candidate.getId());
            if (resumes.isEmpty()) {
                throw new ResourceNotFoundException("Resume", "candidateId", candidate.getId());
            }
            primaryResume = resumes.get(0);
        }

        return candidateMapper.toResumeResponse(primaryResume);
    }

    /**
     * Validate resume file
     */
    private void validateResumeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Please select a file to upload");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new BadRequestException("Filename is required");
        }

        String extension = fileStorageService.getFileExtension(filename).toLowerCase();
        if (!List.of("pdf", "doc", "docx").contains(extension)) {
            throw new BadRequestException("Only PDF, DOC, and DOCX files are allowed");
        }

        // Check file size (10MB max)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("File size exceeds 10MB limit");
        }
    }
}