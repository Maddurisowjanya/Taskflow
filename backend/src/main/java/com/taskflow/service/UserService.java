package com.taskflow.service;

import com.taskflow.dto.TaskFlowDTOs.*;
import com.taskflow.entity.User;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.UserDetailsImpl;
import com.taskflow.entity.Task.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UserService - Business logic for user profile management.
 *
 * Handles:
 * - Getting the current user's profile
 * - Updating profile information
 * - Changing passwords
 * - Admin operations (list all users, deactivate users)
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get the profile of the currently logged-in user.
     *
     * @return UserResponse with profile data and task statistics
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        User user = getCurrentUser();
        return mapToUserResponse(user);
    }

    /**
     * Update the current user's profile.
     * Only updates non-null fields (partial update).
     *
     * @param request new profile data
     * @return updated UserResponse
     */
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    /**
     * Change the current user's password.
     *
     * Steps:
     * 1. Verify current password matches
     * 2. Hash new password
     * 3. Save updated password
     *
     * @param request contains currentPassword and newPassword
     * @return success message
     */
    public String changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        // Verify current password using BCrypt matching
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Hash and save new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return "Password changed successfully";
    }

    /**
     * Admin: Get all users.
     * Only accessible by ROLE_ADMIN (enforced by @PreAuthorize in controller).
     *
     * @return list of all users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }

    /**
     * Admin: Toggle user active status (activate/deactivate).
     *
     * @param userId the user to toggle
     * @return updated user
     */
    public UserResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setIsActive(!user.getIsActive());
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    /**
     * Get the currently authenticated user.
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Map User entity to UserResponse DTO, including task stats.
     */
    private UserResponse mapToUserResponse(User user) {
        Long totalTasks = taskRepository.countByUserId(user.getId());
        Long completedTasks = taskRepository.countByUserIdAndStatus(user.getId(), TaskStatus.COMPLETED);

        List<String> roles = user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toList());

        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .bio(user.getBio())
            .avatarUrl(user.getAvatarUrl())
            .isActive(user.getIsActive())
            .roles(roles)
            .createdAt(user.getCreatedAt())
            .totalTasks(totalTasks)
            .completedTasks(completedTasks)
            .build();
    }
}
