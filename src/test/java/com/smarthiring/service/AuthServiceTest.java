package com.smarthiring.service;

import com.smarthiring.dto.request.LoginRequest;
import com.smarthiring.dto.response.JwtAuthResponse;
import com.smarthiring.entity.Candidate;
import com.smarthiring.entity.User;
import com.smarthiring.enums.RoleName;
import com.smarthiring.repository.CandidateRepository;
import com.smarthiring.repository.RoleRepository;
import com.smarthiring.repository.UserRepository;
import com.smarthiring.security.CustomUserDetails;
import com.smarthiring.security.JwtTokenProvider;
import com.smarthiring.util.TestObjectFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CandidateRepository candidateRepository; // ✅ Added missing mock

    @Mock
    private RoleRepository roleRepository;           // ✅ Added missing mock

    @Mock
    private PasswordEncoder passwordEncoder;         // ✅ Added missing mock

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test@test.com", "password");
        User user = TestObjectFactory.createTestUser(1L, "test@test.com", RoleName.ROLE_CANDIDATE);

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = CustomUserDetails.build(user);

        // Mock authentication manager
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Mock JWT generation
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("mock-jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        // ✅ Mock CandidateRepository behavior (Crucial Fix)
        // Since the user has ROLE_CANDIDATE, the service tries to find their candidate profile
        when(candidateRepository.findByUserId(1L)).thenReturn(Optional.empty());
        // OR return a dummy candidate if you want to test that specific flow:
        // when(candidateRepository.findByUserId(1L)).thenReturn(Optional.of(new Candidate()));

        // Act
        JwtAuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getUser());
    }
}