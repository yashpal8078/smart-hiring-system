package com.smarthiring.mapper;

import com.smarthiring.dto.response.UserResponse;
import com.smarthiring.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    /**
     * Convert User entity to UserResponse DTO
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .profilePicture(user.getProfilePicture())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList()))
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Convert User entity to UserResponse with candidateId
     */
    public UserResponse toResponseWithCandidate(User user, Long candidateId) {
        UserResponse response = toResponse(user);
        if (response != null) {
            response.setCandidateId(candidateId);
        }
        return response;
    }
}