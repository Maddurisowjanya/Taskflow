package com.taskflow.dto;

import com.taskflow.entity.Task.TaskPriority;
import com.taskflow.entity.Task.TaskStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTOs (Data Transfer Objects) - Used to transfer data between layers.
 *
 * We NEVER expose Entity objects directly in APIs because:
 * 1. Entities may have sensitive fields (like password)
 * 2. We can control exactly what data is sent/received
 * 3. Decouples API contract from database structure
 *
 * All DTOs are defined as inner classes here for organization.
 */
public class TaskFlowDTOs {

    // ==========================================
    // AUTH DTOs
    // ==========================================

    /**
     * Request body for user registration.
     * Validation annotations ensure data integrity before hitting the DB.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
        private String password;

        @Size(max = 50)
        private String firstName;

        @Size(max = 50)
        private String lastName;
    }

    /**
     * Request body for user login.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {

        @NotBlank(message = "Username is required")
        private String username;  // Can be username or email

        @NotBlank(message = "Password is required")
        private String password;
    }

    /**
     * Response body after successful login.
     * Contains the JWT token and user details.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JwtResponse {
        private String token;
        private String type = "Bearer";   // Token type for Authorization header
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private List<String> roles;       // e.g., ["ROLE_USER", "ROLE_ADMIN"]
    }

    // ==========================================
    // USER DTOs
    // ==========================================

    /**
     * User profile data returned in API responses.
     * Does NOT include the password!
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String bio;
        private String avatarUrl;
        private Boolean isActive;
        private List<String> roles;
        private LocalDateTime createdAt;
        private Long totalTasks;       // Computed stats
        private Long completedTasks;
    }

    /**
     * Request body for updating user profile.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfileRequest {

        @Size(max = 50, message = "First name cannot exceed 50 characters")
        private String firstName;

        @Size(max = 50, message = "Last name cannot exceed 50 characters")
        private String lastName;

        @Size(max = 500, message = "Bio cannot exceed 500 characters")
        private String bio;

        private String avatarUrl;
    }

    /**
     * Request body for changing password.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {

        @NotBlank(message = "Current password is required")
        private String currentPassword;

        @NotBlank(message = "New password is required")
        @Size(min = 6, max = 100, message = "New password must be 6-100 characters")
        private String newPassword;
    }

    // ==========================================
    // TASK DTOs
    // ==========================================

    /**
     * Request body for creating a new task.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateTaskRequest {

        @NotBlank(message = "Task title is required")
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        private String title;

        private String description;

        private TaskStatus status;      // Defaults to TODO if not provided

        private TaskPriority priority;  // Defaults to MEDIUM if not provided

        @Size(max = 100, message = "Category cannot exceed 100 characters")
        private String category;

        @Future(message = "Due date must be in the future")
        private LocalDate dueDate;
    }

    /**
     * Request body for updating an existing task.
     * All fields are optional (partial update).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateTaskRequest {

        @Size(max = 200, message = "Title cannot exceed 200 characters")
        private String title;

        private String description;

        private TaskStatus status;

        private TaskPriority priority;

        @Size(max = 100, message = "Category cannot exceed 100 characters")
        private String category;

        private LocalDate dueDate;
    }

    /**
     * Task data returned in API responses.
     * Includes computed fields like isOverdue.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskResponse {
        private Long id;
        private String title;
        private String description;
        private TaskStatus status;
        private TaskPriority priority;
        private String category;
        private LocalDate dueDate;
        private LocalDateTime completedAt;
        private Long userId;
        private String username;   // Task owner's username
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Boolean isOverdue; // Computed: dueDate is in the past and not completed
    }

    // ==========================================
    // STATISTICS / DASHBOARD DTOs
    // ==========================================

    /**
     * Dashboard statistics for the logged-in user.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStats {
        private Long totalTasks;
        private Long todoTasks;
        private Long inProgressTasks;
        private Long completedTasks;
        private Long cancelledTasks;
        private Long overdueTasks;
        private Long urgentTasks;
        private Double completionRate;   // completedTasks / totalTasks * 100
        private Map<String, Long> tasksByStatus;
        private Map<String, Long> tasksByPriority;
        private Map<String, Long> tasksByCategory;
        private List<TaskResponse> recentTasks;
        private List<TaskResponse> upcomingDueTasks;
    }

    // ==========================================
    // GENERIC RESPONSE WRAPPER
    // ==========================================

    /**
     * Generic API response wrapper.
     * Provides a consistent response format for all API calls.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse<T> {
        private Boolean success;
        private String message;
        private T data;
        private LocalDateTime timestamp;

        /** Factory method for success response */
        public static <T> ApiResponse<T> success(String message, T data) {
            return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        }

        /** Factory method for error response */
        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        }
    }
}
