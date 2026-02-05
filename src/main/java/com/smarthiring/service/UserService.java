package com.smarthiring.service;

import com.smarthiring.dto.response.PagedResponse;
import com.smarthiring.dto.response.UserResponse;
import com.smarthiring.entity.User;
import com.smarthiring.enums.RoleName;
import com.smarthiring.exception.BadRequestException;
import com.smarthiring.exception.ResourceNotFoundException;
import com.smarthiring.mapper.UserMapper;
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
public class UserService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final UserMapper userMapper;

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        UserResponse response = userMapper.toResponse(user);

        // Add candidateId if candidate
        candidateRepository.findByUserId(id)
                .ifPresent(c -> response.setCandidateId(c.getId()));

        return response;
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return userMapper.toResponse(user);
    }

    /**
     * Get all users (Admin only)
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> usersPage = userRepository.findAll(pageable);

        List<UserResponse> content = usersPage.getContent().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages()
        );
    }

    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getUsersByRole(String roleName, int page, int size) {
        RoleName role;
        try {
            role = RoleName.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + roleName);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> usersPage = userRepository.findByRoleName(role, pageable);

        List<UserResponse> content = usersPage.getContent().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages()
        );
    }

    /**
     * Search users
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.searchUsers(keyword, pageable);

        List<UserResponse> content = usersPage.getContent().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                content,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages()
        );
    }

    /**
     * Activate/Deactivate user
     */
    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setIsActive(active);
        User savedUser = userRepository.save(user);

        log.info("User {} status updated to: {}", userId, active ? "Active" : "Inactive");

        return userMapper.toResponse(savedUser);
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userRepository.delete(user);
        log.info("User deleted: {}", userId);
    }

    /**
     * Update user profile picture
     */
    @Transactional
    public UserResponse updateProfilePicture(Long userId, String pictureUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setProfilePicture(pictureUrl);
        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    /**
     * Count users by role
     */
    @Transactional(readOnly = true)
    public long countUsersByRole(RoleName roleName) {
        return userRepository.countByRole(roleName);
    }

    /**
     * Count active users
     */
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.countByIsActiveTrue();
    }
}