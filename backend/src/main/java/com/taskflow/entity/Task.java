package com.taskflow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Task Entity - Represents a task created by a user.
 *
 * Contains all the fields needed to manage a task:
 * title, description, status, priority, category, due date, etc.
 */
@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Task title - required, max 200 characters */
    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    /** Detailed description of the task (optional) */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Task Status - current state of the task.
     *
     * TODO → IN_PROGRESS → COMPLETED (or CANCELLED at any stage)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.TODO;

    /**
     * Task Priority - urgency level of the task.
     * Helps users sort and focus on important work.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    /** Category tag (e.g., "Work", "Personal", "Shopping") */
    @Column(length = 100)
    private String category;

    /** Deadline for completing the task */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /** Timestamp when the task was marked as COMPLETED */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Many-to-One relationship with User.
     * Many tasks belong to one user.
     *
     * @ManyToMany + @JoinColumn: creates a 'user_id' foreign key in the tasks table
     * FetchType.LAZY: user is loaded only when accessed
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude   // Avoid circular toString
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==========================================
    // Enum Definitions
    // ==========================================

    /**
     * TaskStatus - Lifecycle states of a task.
     */
    public enum TaskStatus {
        TODO,         // Not started yet
        IN_PROGRESS,  // Currently being worked on
        COMPLETED,    // Successfully finished
        CANCELLED     // Abandoned/no longer needed
    }

    /**
     * TaskPriority - Urgency levels for tasks.
     */
    public enum TaskPriority {
        LOW,    // Nice to do, not urgent
        MEDIUM, // Normal priority
        HIGH,   // Important, should be done soon
        URGENT  // Critical, must be done immediately
    }
}
