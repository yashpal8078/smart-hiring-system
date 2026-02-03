package com.smarthiring.service;

import com.smarthiring.dto.request.*;
import com.smarthiring.dto.response.JwtAuthResponse;
import com.smarthiring.dto.response.UserResponse;
import com.smarthiring.entity.Candidate;
import com.smarthiring.entity.Role;
import com.smarthiring.entity.User;
import com.smarthiring.enums.RoleName;
import com.smarthiring.exception.BadRequestException;
import com.smarthiring.exception.ResourceNotFoundException;
import com.smarthiring.repository.CandidateRepository;
import com.smarthiring.repository.RoleRepository;
import com.smarthiring.repository.UserRepository;
import com.smarthiring.security.CustomUserDetails;
import com.smarthiring.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticate user and generate JWT token
     */
    @Transactional
    public JwtAuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(authentication);

            // Update last login
            userRepository.updateLastLogin(userDetails.getId(), LocalDateTime.now());

            // Get roles
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.toList());

            // Build user response
            UserResponse userResponse = buildUserResponse(userDetails);

            log.info("Login successful for user: {}", request.getEmail());

            return JwtAuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationTime())
                    .user(userResponse)
                    .roles(roles)
                    .build();

        } catch (BadCredentialsException e) {
            log.error("Invalid credentials for email: {}", request.getEmail());
            throw new BadRequestException("Invalid email or password");
        }
    }

    /**
     * Register a new user
     */
    @Transactional
    public JwtAuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        // Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Get role
        RoleName roleName = request.getRole().equalsIgnoreCase("HR")
                ? RoleName.ROLE_HR
                : RoleName.ROLE_CANDIDATE;

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName.name()));

        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .isActive(true)
                .emailVerified(false)
                .build();

        user.addRole(role);

        // Save user
        User savedUser = userRepository.save(user);

        // If candidate, create candidate profile
        if (roleName == RoleName.ROLE_CANDIDATE) {
            Candidate candidate = Candidate.builder()
                    .user(savedUser)
                    .build();
            candidateRepository.save(candidate);
        }

        // Generate token
        String roles = savedUser.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.joining(","));

        String token = jwtTokenProvider.generateTokenForUser(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                roles
        );

        // Build response
        List<String> roleList = savedUser.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        UserResponse userResponse = UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .phone(savedUser.getPhone())
                .isActive(savedUser.getIsActive())
                .emailVerified(savedUser.getEmailVerified())
                .roles(roleList)
                .createdAt(savedUser.getCreatedAt())
                .build();

        // Add candidateId if candidate
        if (roleName == RoleName.ROLE_CANDIDATE) {
            candidateRepository.findByUserId(savedUser.getId())
                    .ifPresent(c -> userResponse.setCandidateId(c.getId()));
        }

        log.info("Registration successful for user: {}", request.getEmail());

        return JwtAuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .user(userResponse)
                .roles(roleList)
                .build();
    }

    /**
     * Change password for authenticated user
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Password change attempt for user ID: {}", userId);

        // Validate new password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New passwords do not match");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Check if new password is same as current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user ID: {}", userId);
    }

    /**
     * Get current authenticated user
     */
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("User not authenticated");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return buildUserResponse(userDetails);
    }

    /**
     * Build UserResponse from CustomUserDetails
     */
    private UserResponse buildUserResponse(CustomUserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList());

        UserResponse response = UserResponse.builder()
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .fullName(userDetails.getFullName())
                .isActive(userDetails.getIsActive())
                .roles(roles)
                .build();

        // Add candidateId if candidate
        if (userDetails.hasRole("ROLE_CANDIDATE")) {
            candidateRepository.findByUserId(userDetails.getId())
                    .ifPresent(c -> response.setCandidateId(c.getId()));
        }

        return response;
    }

    /**
     * Validate token and return user info
     */
    public UserResponse validateToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadRequestException("Invalid or expired token");
        }

        String email = jwtTokenProvider.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .profilePicture(user.getProfilePicture())
                .roles(roles)
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();

        // Add candidateId if candidate
        candidateRepository.findByUserId(user.getId())
                .ifPresent(c -> response.setCandidateId(c.getId()));

        return response;
    }
    /**
     * Check if email already exists
     */
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}