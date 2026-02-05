package com.smarthiring.service;

import com.smarthiring.dto.request.CandidateProfileRequest;
import com.smarthiring.dto.request.CandidateSearchRequest;
import com.smarthiring.dto.response.CandidateResponse;
import com.smarthiring.dto.response.PagedResponse;
import com.smarthiring.entity.Candidate;
import com.smarthiring.entity.User;
import com.smarthiring.exception.ResourceNotFoundException;
import com.smarthiring.mapper.CandidateMapper;
import com.smarthiring.repository.CandidateRepository;
import com.smarthiring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final CandidateMapper candidateMapper;

    /**
     * Get candidate by ID
     */
    @Transactional(readOnly = true)
    public CandidateResponse getCandidateById(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", id));

        return candidateMapper.toResponse(candidate);
    }

    /**
     * Get candidate by user ID
     */
    @Transactional(readOnly = true)
    public CandidateResponse getCandidateByUserId(Long userId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "userId", userId));

        return candidateMapper.toResponse(candidate);
    }

    /**
     * Get candidate by email
     */
    @Transactional(readOnly = true)
    public CandidateResponse getCandidateByEmail(String email) {
        Candidate candidate = candidateRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "email", email));

        return candidateMapper.toResponse(candidate);
    }

    /**
     * Update candidate profile
     */
    @Transactional
    public CandidateResponse updateProfile(Long userId, CandidateProfileRequest request) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "userId", userId));

        candidateMapper.updateFromRequest(candidate, request);

        Candidate savedCandidate = candidateRepository.save(candidate);

        // Also save user if user fields were updated
        if (request.getFullName() != null || request.getPhone() != null) {
            userRepository.save(candidate.getUser());
        }

        log.info("Candidate profile updated for user: {}", userId);

        return candidateMapper.toResponse(savedCandidate);
    }

    /**
     * Get all candidates with pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<CandidateResponse> getAllCandidates(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Candidate> candidatesPage = candidateRepository.findAll(pageable);

        List<CandidateResponse> content = candidatesPage.getContent().stream()
                .map(candidateMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                candidatesPage.getNumber(),
                candidatesPage.getSize(),
                candidatesPage.getTotalElements(),
                candidatesPage.getTotalPages()
        );
    }

    /**
     * Search candidates
     */
    @Transactional(readOnly = true)
    public PagedResponse<CandidateResponse> searchCandidates(CandidateSearchRequest request) {
        Sort sort = request.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(request.getSortBy()).descending()
                : Sort.by(request.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Candidate> candidatesPage = candidateRepository.searchCandidates(
                request.getLocation(),
                request.getMinExperience(),
                request.getMaxExperience(),
                request.getSkills(),
                pageable
        );

        List<CandidateResponse> content = candidatesPage.getContent().stream()
                .map(candidateMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                candidatesPage.getNumber(),
                candidatesPage.getSize(),
                candidatesPage.getTotalElements(),
                candidatesPage.getTotalPages()
        );
    }

    /**
     * Get candidates by skill
     */
    @Transactional(readOnly = true)
    public PagedResponse<CandidateResponse> getCandidatesBySkill(String skill, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("totalExperience").descending());

        Page<Candidate> candidatesPage = candidateRepository.findBySkillContaining(skill, pageable);

        List<CandidateResponse> content = candidatesPage.getContent().stream()
                .map(candidateMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                candidatesPage.getNumber(),
                candidatesPage.getSize(),
                candidatesPage.getTotalElements(),
                candidatesPage.getTotalPages()
        );
    }

    /**
     * Get candidates by location
     */
    @Transactional(readOnly = true)
    public PagedResponse<CandidateResponse> getCandidatesByLocation(String location, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Candidate> candidatesPage = candidateRepository.findByLocationIgnoreCase(location, pageable);

        List<CandidateResponse> content = candidatesPage.getContent().stream()
                .map(candidateMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                candidatesPage.getNumber(),
                candidatesPage.getSize(),
                candidatesPage.getTotalElements(),
                candidatesPage.getTotalPages()
        );
    }

    /**
     * Get all distinct locations
     */
    @Transactional(readOnly = true)
    public List<String> getAllLocations() {
        return candidateRepository.findAllDistinctLocations();
    }

    /**
     * Count total candidates
     */
    @Transactional(readOnly = true)
    public long countCandidates() {
        return candidateRepository.count();
    }
}