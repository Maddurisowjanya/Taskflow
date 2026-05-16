package com.taskflow.repository;

import com.taskflow.entity.Task;
import com.taskflow.entity.Task.TaskPriority;
import com.taskflow.entity.Task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * TaskRepository - Data Access Layer for Task entity.
 *
 * Extends JpaRepository for built-in CRUD.
 * Adds custom methods for filtering, searching, and statistics.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Get all tasks belonging to a specific user.
     * Used for the main task list page.
     */
    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Filter tasks by user and status.
     * e.g., show only "IN_PROGRESS" tasks for a user.
     */
    List<Task> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, TaskStatus status);

    /**
     * Filter tasks by user and priority.
     * e.g., show only "URGENT" tasks for a user.
     */
    List<Task> findByUserIdAndPriorityOrderByCreatedAtDesc(Long userId, TaskPriority priority);

    /**
     * Filter tasks by user and category.
     * e.g., show only "Work" category tasks.
     */
    List<Task> findByUserIdAndCategoryOrderByCreatedAtDesc(Long userId, String category);

    /**
     * Search tasks by title containing a keyword (case-insensitive).
     * Uses SQL LIKE under the hood.
     *
     * @param userId the user's ID
     * @param keyword the search term
     */
    List<Task> findByUserIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
        Long userId, String keyword);

    /**
     * Find tasks due before a specific date (overdue tasks).
     * Useful for sending reminders or showing overdue tasks.
     */
    List<Task> findByUserIdAndDueDateBeforeAndStatusNot(
        Long userId, LocalDate date, TaskStatus status);

    /**
     * Count tasks by status for a specific user.
     * Used for dashboard statistics.
     */
    Long countByUserIdAndStatus(Long userId, TaskStatus status);

    /**
     * Count total tasks for a user.
     */
    Long countByUserId(Long userId);

    /**
     * Custom JPQL query to get task count grouped by status.
     * Returns a list of [status, count] arrays for chart data.
     *
     * @param userId the user's ID
     */
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.user.id = :userId GROUP BY t.status")
    List<Object[]> countByUserIdGroupByStatus(@Param("userId") Long userId);

    /**
     * Custom JPQL query to get task count grouped by priority.
     * Used for priority distribution chart.
     */
    @Query("SELECT t.priority, COUNT(t) FROM Task t WHERE t.user.id = :userId GROUP BY t.priority")
    List<Object[]> countByUserIdGroupByPriority(@Param("userId") Long userId);

    /**
     * Get tasks created in the last N days for trend analysis.
     * Uses a native SQL query for the date calculation.
     */
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.createdAt >= :startDate ORDER BY t.createdAt")
    List<Task> findRecentTasksByUserId(@Param("userId") Long userId, @Param("startDate") java.time.LocalDateTime startDate);

    /**
     * Combined filter: status + priority for a user.
     */
    List<Task> findByUserIdAndStatusAndPriorityOrderByCreatedAtDesc(
        Long userId, TaskStatus status, TaskPriority priority);

    /**
     * Admin: Get all tasks (for admin dashboard).
     */
    @Query("SELECT t FROM Task t ORDER BY t.createdAt DESC")
    List<Task> findAllTasksForAdmin();
}
