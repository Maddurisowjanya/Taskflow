package com.taskflow.service;

import com.taskflow.dto.TaskFlowDTOs.*;
import com.taskflow.entity.Task;
import com.taskflow.entity.Task.TaskStatus;
import com.taskflow.entity.Task.TaskPriority;
import com.taskflow.entity.User;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TaskService - Business logic for all task-related operations.
 *
 * Contains:
 * - CRUD operations for tasks
 * - Filtering and searching
 * - Dashboard statistics
 * - Authorization checks (users can only manage their own tasks)
 */
@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    // ==========================================
    // CRUD Operations
    // ==========================================

    /**
     * Create a new task for the current user.
     *
     * @param request task creation data
     * @return created task response
     */
    public TaskResponse createTask(CreateTaskRequest request) {
        User currentUser = getCurrentUser();

        Task task = Task.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
            .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
            .category(request.getCategory())
            .dueDate(request.getDueDate())
            .user(currentUser)
            .build();

        Task savedTask = taskRepository.save(task);
        return mapToTaskResponse(savedTask);
    }

    /**
     * Get all tasks for the current user.
     *
     * @return list of task responses
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasksForCurrentUser() {
        User currentUser = getCurrentUser();
        List<Task> tasks = taskRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        return tasks.stream().map(this::mapToTaskResponse).collect(Collectors.toList());
    }

    /**
     * Get a specific task by ID.
     * Only the task owner (or admin) can access it.
     *
     * @param taskId the task's ID
     * @return task response
     */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long taskId) {
        Task task = findTaskById(taskId);
        checkTaskOwnership(task);
        return mapToTaskResponse(task);
    }

    /**
     * Update an existing task.
     * Only the task owner can update it.
     * Only non-null fields in the request are updated (partial update).
     *
     * @param taskId the task's ID
     * @param request updated task data
     * @return updated task response
     */
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        Task task = findTaskById(taskId);
        checkTaskOwnership(task);

        // Update only fields that are provided in the request (partial update)
        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            // If marking as completed, set completedAt timestamp
            if (request.getStatus() == TaskStatus.COMPLETED && task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            }
            // If un-completing, clear completedAt
            if (request.getStatus() != TaskStatus.COMPLETED) {
                task.setCompletedAt(null);
            }
        }
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getCategory() != null) task.setCategory(request.getCategory());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());

        Task updatedTask = taskRepository.save(task);
        return mapToTaskResponse(updatedTask);
    }

    /**
     * Delete a task.
     * Only the task owner can delete it.
     *
     * @param taskId the task's ID
     */
    public void deleteTask(Long taskId) {
        Task task = findTaskById(taskId);
        checkTaskOwnership(task);
        taskRepository.delete(task);
    }

    /**
     * Mark a task as completed.
     * Convenience endpoint for the "complete" button.
     *
     * @param taskId the task's ID
     * @return updated task response
     */
    public TaskResponse markTaskCompleted(Long taskId) {
        Task task = findTaskById(taskId);
        checkTaskOwnership(task);

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);
        return mapToTaskResponse(updatedTask);
    }

    // ==========================================
    // Filtering and Searching
    // ==========================================

    /**
     * Filter tasks by status.
     *
     * @param status the TaskStatus to filter by
     * @return filtered task list
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        User currentUser = getCurrentUser();
        return taskRepository
            .findByUserIdAndStatusOrderByCreatedAtDesc(currentUser.getId(), status)
            .stream().map(this::mapToTaskResponse).collect(Collectors.toList());
    }

    /**
     * Filter tasks by priority.
     *
     * @param priority the TaskPriority to filter by
     * @return filtered task list
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByPriority(TaskPriority priority) {
        User currentUser = getCurrentUser();
        return taskRepository
            .findByUserIdAndPriorityOrderByCreatedAtDesc(currentUser.getId(), priority)
            .stream().map(this::mapToTaskResponse).collect(Collectors.toList());
    }

    /**
     * Filter tasks by category.
     *
     * @param category the category string to filter by
     * @return filtered task list
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByCategory(String category) {
        User currentUser = getCurrentUser();
        return taskRepository
            .findByUserIdAndCategoryOrderByCreatedAtDesc(currentUser.getId(), category)
            .stream().map(this::mapToTaskResponse).collect(Collectors.toList());
    }

    /**
     * Search tasks by title keyword.
     *
     * @param keyword the search term
     * @return matching tasks
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> searchTasks(String keyword) {
        User currentUser = getCurrentUser();
        return taskRepository
            .findByUserIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                currentUser.getId(), keyword)
            .stream().map(this::mapToTaskResponse).collect(Collectors.toList());
    }

    // ==========================================
    // Dashboard Statistics
    // ==========================================

    /**
     * Get comprehensive dashboard statistics for the current user.
     * Used to populate charts and stat cards on the dashboard.
     *
     * @return DashboardStats with all metrics
     */
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        User currentUser = getCurrentUser();
        Long userId = currentUser.getId();

        // Count tasks by status
        Long totalTasks = taskRepository.countByUserId(userId);
        Long todoTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.TODO);
        Long inProgressTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.IN_PROGRESS);
        Long completedTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.COMPLETED);
        Long cancelledTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.CANCELLED);
        Long urgentTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.TODO); // placeholder

        // Count overdue tasks
        List<Task> overdueTasks = taskRepository.findByUserIdAndDueDateBeforeAndStatusNot(
            userId, LocalDate.now(), TaskStatus.COMPLETED);
        Long overdueCount = (long) overdueTasks.size();

        // Completion rate
        double completionRate = totalTasks > 0
            ? Math.round((completedTasks.doubleValue() / totalTasks) * 100.0 * 10) / 10.0
            : 0.0;

        // Tasks by status (for bar/pie chart)
        Map<String, Long> tasksByStatus = new LinkedHashMap<>();
        tasksByStatus.put("TODO", todoTasks);
        tasksByStatus.put("IN_PROGRESS", inProgressTasks);
        tasksByStatus.put("COMPLETED", completedTasks);
        tasksByStatus.put("CANCELLED", cancelledTasks);

        // Tasks by priority (for pie chart)
        Map<String, Long> tasksByPriority = new LinkedHashMap<>();
        for (Object[] row : taskRepository.countByUserIdGroupByPriority(userId)) {
            tasksByPriority.put(row[0].toString(), (Long) row[1]);
        }

        // Tasks by category (for horizontal bar chart)
        List<Task> allTasks = taskRepository.findByUserIdOrderByCreatedAtDesc(userId);
        Map<String, Long> tasksByCategory = allTasks.stream()
            .filter(t -> t.getCategory() != null && !t.getCategory().isEmpty())
            .collect(Collectors.groupingBy(Task::getCategory, Collectors.counting()));

        // Recent 5 tasks
        List<TaskResponse> recentTasks = allTasks.stream()
            .limit(5)
            .map(this::mapToTaskResponse)
            .collect(Collectors.toList());

        // Upcoming due tasks (next 7 days, not completed)
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        List<TaskResponse> upcomingDueTasks = allTasks.stream()
            .filter(t -> t.getDueDate() != null
                && !t.getDueDate().isBefore(today)
                && !t.getDueDate().isAfter(nextWeek)
                && t.getStatus() != TaskStatus.COMPLETED)
            .map(this::mapToTaskResponse)
            .collect(Collectors.toList());

        return DashboardStats.builder()
            .totalTasks(totalTasks)
            .todoTasks(todoTasks)
            .inProgressTasks(inProgressTasks)
            .completedTasks(completedTasks)
            .cancelledTasks(cancelledTasks)
            .overdueTasks(overdueCount)
            .urgentTasks(urgentTasks)
            .completionRate(completionRate)
            .tasksByStatus(tasksByStatus)
            .tasksByPriority(tasksByPriority)
            .tasksByCategory(tasksByCategory)
            .recentTasks(recentTasks)
            .upcomingDueTasks(upcomingDueTasks)
            .build();
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    /**
     * Get the currently authenticated user from the security context.
     *
     * @return the logged-in User entity
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    /**
     * Find a task by ID, throwing 404 if not found.
     *
     * @param taskId the task ID
     * @return the Task entity
     */
    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
    }

    /**
     * Check that the current user owns the task (or is an admin).
     * Throws 403 if not authorized.
     *
     * @param task the task to check ownership for
     */
    private void checkTaskOwnership(Task task) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        boolean isAdmin = userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !task.getUser().getId().equals(userDetails.getId())) {
            throw new AccessDeniedException("You don't have permission to access this task");
        }
    }

    /**
     * Convert a Task entity to TaskResponse DTO.
     * This is the "mapping" step that decouples entities from API responses.
     *
     * @param task the Task entity
     * @return TaskResponse DTO
     */
    private TaskResponse mapToTaskResponse(Task task) {
        boolean isOverdue = task.getDueDate() != null
            && task.getDueDate().isBefore(LocalDate.now())
            && task.getStatus() != TaskStatus.COMPLETED
            && task.getStatus() != TaskStatus.CANCELLED;

        return TaskResponse.builder()
            .id(task.getId())
            .title(task.getTitle())
            .description(task.getDescription())
            .status(task.getStatus())
            .priority(task.getPriority())
            .category(task.getCategory())
            .dueDate(task.getDueDate())
            .completedAt(task.getCompletedAt())
            .userId(task.getUser().getId())
            .username(task.getUser().getUsername())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .isOverdue(isOverdue)
            .build();
    }
}
