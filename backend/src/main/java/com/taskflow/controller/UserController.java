package com.taskflow.controller;

import com.taskflow.dto.TaskFlowDTOs.*;
import com.taskflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserController - REST API endpoints for user profile management.
 *
 * Base URL: /api/users
 * All endpoints require JWT authentication.
 *
 * Includes both user and admin endpoints.
 * Admin endpoints are protected by @PreAuthorize("hasRole('ADMIN')")
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Users", description = "User profile management APIs")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    @Autowired
    private UserService userService;

    // ==========================================
    // USER PROFILE ENDPOINTS
    // ==========================================

    /**
     * GET /api/users/profile - Get current user's profile.
     * Returns profile info + task statistics.
     */
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        UserResponse user = userService.getCurrentUserProfile();
        return ResponseEntity.ok(
            ApiResponse.success("Profile retrieved successfully", user)
        );
    }

    /**
     * PUT /api/users/profile - Update current user's profile.
     *
     * Request body:
     * {
     *   "firstName": "John",
     *   "lastName": "Smith",
     *   "bio": "Full Stack Developer",
     *   "avatarUrl": "https://example.com/avatar.jpg"
     * }
     */
    @PutMapping("/profile")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
        @Valid @RequestBody UpdateProfileRequest request) {

        UserResponse user = userService.updateProfile(request);
        return ResponseEntity.ok(
            ApiResponse.success("Profile updated successfully", user)
        );
    }

    /**
     * PATCH /api/users/change-password - Change current user's password.
     *
     * Request body:
     * {
     *   "currentPassword": "oldpassword",
     *   "newPassword": "newpassword123"
     * }
     */
    @PatchMapping("/change-password")
    @Operation(summary = "Change current user's password")
    public ResponseEntity<ApiResponse<String>> changePassword(
        @Valid @RequestBody ChangePasswordRequest request) {

        String message = userService.changePassword(request);
        return ResponseEntity.ok(
            ApiResponse.success(message, null)
        );
    }

    // ==========================================
    // ADMIN ENDPOINTS
    // ==========================================

    /**
     * GET /api/users - Get all users (Admin only).
     *
     * @PreAuthorize("hasRole('ADMIN')") - Only ROLE_ADMIN can access this.
     * Returns 403 Forbidden if a regular user tries to access.
     */
    @GetMapping
    @Operation(summary = "Get all users (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(
            ApiResponse.success("Users retrieved successfully", users)
        );
    }

    /**
     * PATCH /api/users/{id}/toggle-status - Activate/deactivate user (Admin only).
     * Toggles the isActive flag on the user account.
     */
    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle user active status (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(
        @PathVariable Long id) {

        UserResponse user = userService.toggleUserStatus(id);
        return ResponseEntity.ok(
            ApiResponse.success("User status updated", user)
        );
    }
}
