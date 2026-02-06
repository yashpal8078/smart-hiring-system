package com.smarthiring.config;

import com.smarthiring.entity.Candidate;
import com.smarthiring.entity.Role;
import com.smarthiring.entity.User;
import com.smarthiring.enums.RoleName;
import com.smarthiring.repository.CandidateRepository;
import com.smarthiring.repository.RoleRepository;
import com.smarthiring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("========================================");
        log.info("Initializing database with default data...");
        log.info("========================================");

        // Create roles
        createRoles();

        // Create default users
        createDefaultUsers();

        log.info("========================================");
        log.info("Database initialization completed!");
        log.info("========================================");
    }

    private void createRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role();
                role.setName(roleName);
                role.setDescription(getRoleDescription(roleName));
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
            }
        }
    }

    private String getRoleDescription(RoleName roleName) {
        return switch (roleName) {
            case ROLE_ADMIN -> "System Administrator with full access";
            case ROLE_HR -> "HR Manager who can post jobs and manage candidates";
            case ROLE_CANDIDATE -> "Job seeker who can apply for jobs";
        };
    }

    private void createDefaultUsers() {
        // Create Admin user
        createUserIfNotExists(
                "admin@smarthiring.com",
                "Admin@123",
                "System Admin",
                "9999999999",
                RoleName.ROLE_ADMIN
        );

        // Create HR user
        createUserIfNotExists(
                "hr@smarthiring.com",
                "Hr@12345",
                "HR Manager",
                "8888888888",
                RoleName.ROLE_HR
        );

        // Create Demo Candidate user
        createCandidateIfNotExists(
                "candidate@smarthiring.com",
                "Candidate@123",
                "John Doe",
                "7777777777"
        );
    }

    private void createUserIfNotExists(String email, String password, String fullName,
                                       String phone, RoleName roleName) {
        if (!userRepository.existsByEmail(email)) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .fullName(fullName)
                    .phone(phone)
                    .isActive(true)
                    .emailVerified(true)
                    .build();

            user.addRole(role);
            userRepository.save(user);

            log.info("Created user: {} with role: {}", email, roleName);
        } else {
            log.info("User already exists: {}", email);
        }
    }

    private void createCandidateIfNotExists(String email, String password, String fullName, String phone) {
        if (!userRepository.existsByEmail(email)) {
            Role role = roleRepository.findByName(RoleName.ROLE_CANDIDATE)
                    .orElseThrow(() -> new RuntimeException("Role not found: ROLE_CANDIDATE"));

            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .fullName(fullName)
                    .phone(phone)
                    .isActive(true)
                    .emailVerified(true)
                    .build();

            user.addRole(role);
            User savedUser = userRepository.save(user);

            // Create candidate profile
            Candidate candidate = Candidate.builder()
                    .user(savedUser)
                    .headline("Experienced Software Developer")
                    .skills("Java, Spring Boot, React, MySQL")
                    .location("Mumbai")
                    .build();

            candidateRepository.save(candidate);

            log.info("Created candidate user: {}", email);
        } else {
            log.info("Candidate already exists: {}", email);
        }
    }
}