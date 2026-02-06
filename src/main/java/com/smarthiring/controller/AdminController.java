package com.smarthiring.controller;

import com.smarthiring.dto.response.ApiResponse;
import com.smarthiring.dto.response.PagedResponse;
import com.smarthiring.dto.response.UserResponse;
import com.smarthiring.service.JobService;
import com.smarthiring.service.UserService;
import com.smarthiring.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Admin Management APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final JobService jobService;

    /**
     * Get all users
     */
    @GetMapping("/users")
    @Operation(summary = "Get All Users", description = "Get all users with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        log.info("Admin: Get all users - page: {}, size: {}", page, size);

        PagedResponse<UserResponse> response = userService.getAllUsers(page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get users by role
     */
    @GetMapping("/users/role/{roleName}")
    @Operation(summary = "Get Users by Role", description = "Get users by role name")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getUsersByRole(
            @PathVariable String roleName,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        log.info("Admin: Get users by role: {}", roleName);

        PagedResponse<UserResponse> response = userService.getUsersByRole(roleName, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Search users
     */
    @GetMapping("/users/search")
    @Operation(summary = "Search Users", description = "Search users by name or email")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        log.info("Admin: Search users with keyword: {}", keyword);

        PagedResponse<UserResponse> response = userService.searchUsers(keyword, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Activate/Deactivate user
     */
    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Update User Status", description = "Activate or deactivate a user")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        log.info("Admin: Update user {} status to: {}", id, active);

        UserResponse response = userService.updateUserStatus(id, active);

        String message = active ? "User activated successfully" : "User deactivated successfully";

        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * Delete user
     */
    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete User", description = "Delete a user permanently")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id
    ) {
        log.info("Admin: Delete user: {}", id);

        userService.deleteUser(id);

        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    /**
     * Get system statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get System Stats", description = "Get overall system statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStats() {
        log.info("Admin: Get system stats");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.countActiveUsers());
        stats.put("totalJobs", jobService.countActiveJobs());

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Deactivate expired jobs (manual trigger)
     */
    @PostMapping("/jobs/deactivate-expired")
    @Operation(summary = "Deactivate Expired Jobs", description = "Manually trigger deactivation of expired jobs")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> deactivateExpiredJobs() {
        log.info("Admin: Deactivate expired jobs");

        int count = jobService.deactivateExpiredJobs();

        Map<String, Integer> result = new HashMap<>();
        result.put("deactivatedCount", count);

        return ResponseEntity.ok(ApiResponse.success("Expired jobs deactivated", result));
    }
}