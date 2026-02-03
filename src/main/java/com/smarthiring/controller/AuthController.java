package com.smarthiring.controller;

import com.smarthiring.dto.request.ChangePasswordRequest;
import com.smarthiring.dto.request.LoginRequest;
import com.smarthiring.dto.request.RegisterRequest;
import com.smarthiring.dto.response.ApiResponse;
import com.smarthiring.dto.response.JwtAuthResponse;
import com.smarthiring.dto.response.UserResponse;
import com.smarthiring.exception.UnauthorizedException;
import com.smarthiring.security.CustomUserDetails;
import com.smarthiring.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and Authorization APIs")
public class AuthController {

    private final AuthService authService;

    /**
     * User Login
     */
    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticate user and get JWT token")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login request received for email: {}", request.getEmail());

        JwtAuthResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * User Registration
     */
    @PostMapping("/register")
    @Operation(summary = "User Registration", description = "Register a new user (Candidate or HR)")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        log.info("Registration request received for email: {}", request.getEmail());

        JwtAuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    /**
     * Get Current User
     */
    @GetMapping("/me")
    @Operation(summary = "Get Current User", description = "Get details of currently logged in user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Please login to access this resource");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("Get current user request for: {}", userDetails.getEmail());

        UserResponse response = authService.getCurrentUser();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Change Password
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change Password", description = "Change password for authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Please login to access this resource");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("Change password request for user: {}", userDetails.getEmail());

        authService.changePassword(userDetails.getId(), request);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    /**
     * Validate Token
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate Token", description = "Validate JWT token and get user info")
    public ResponseEntity<ApiResponse<UserResponse>> validateToken(
            @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid authorization header");
        }

        String token = authHeader.substring(7);

        UserResponse response = authService.validateToken(token);

        return ResponseEntity.ok(ApiResponse.success("Token is valid", response));
    }

    /**
     * Logout
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout user (client should remove token)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<String>> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            log.info("Logout request for user: {}", userDetails.getEmail());
        }

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    /**
     * Check if email exists
     */
    @GetMapping("/check-email")
    @Operation(summary = "Check Email", description = "Check if email is already registered")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(
            @RequestParam String email
    ) {
        boolean exists = authService.checkEmailExists(email);
        return ResponseEntity.ok(ApiResponse.success("Email check completed", exists));
    }
}