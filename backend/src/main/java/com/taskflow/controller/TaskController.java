package com.taskflow.controller;

import com.taskflow.dto.TaskFlowDTOs.*;
import com.taskflow.entity.Task.TaskPriority;
import com.taskflow.entity.Task.TaskStatus;
import com.taskflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TaskController - REST API endpoints for task management.
 *
 * Base URL: /api/tasks
 * All endpoints require JWT authentication.
 *
 * HTTP Method conventions:
 * - GET    = Read data (safe, idempotent)
 * - POST   = Create new resource
 * - PUT    = Replace entire resource
 * - PATCH  = Partially update resource
 * - DELETE = Remove resource
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Tasks", description = "Task management APIs - Create, Read, Update, Delete tasks")
@SecurityRequirement(name = "Bearer Authentication")  // Show lock icon in Swagger
public class TaskController {

    @Autowired
    private TaskService taskService;

    // ==========================================
    // CREATE
    // ==========================================

    /**
     * POST /api/tasks - Create a new task.
     *
     * Request body:
     * {
     *   "title": "Complete project report",
     *   "description": "Write the Q3 analysis",
     *   "priority": "HIGH",
     *   "category": "Work",
     *   "dueDate": "2024-12-31"
     * }
     */
    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
        @Valid @RequestBody CreateTaskRequest request) {

        TaskResponse task = taskService.createTask(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Task created successfully", task));
    }

    // ==========================================
    // READ
    // ==========================================

    /**
     * GET /api/tasks - Get all tasks for the current user.
     * Supports optional filters via query parameters.
     *
     * Examples:
     * GET /api/tasks                           → all tasks
     * GET /api/tasks?status=TODO               → only TODO tasks
     * GET /api/tasks?priority=HIGH             → only HIGH priority tasks
     * GET /api/tasks?category=Work             → only Work category tasks
     * GET /api/tasks?search=project+report     → search by title
     *
     * @param status   filter by status
     * @param priority filter by priority
     * @param category filter by category
     * @param search   search keyword for title
     */
    @GetMapping
    @Operation(summary = "Get all tasks", description = "Returns tasks with optional filters")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAllTasks(
        @RequestParam(required = false) TaskStatus status,
        @RequestParam(required = false) TaskPriority priority,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String search) {

        List<TaskResponse> tasks;

        // Apply filters based on what parameters are provided
        if (search != null && !search.trim().isEmpty()) {
            tasks = taskService.searchTasks(search.trim());
        } else if (status != null) {
            tasks = taskService.getTasksByStatus(status);
        } else if (priority != null) {
            tasks = taskService.getTasksByPriority(priority);
        } else if (category != null) {
            tasks = taskService.getTasksByCategory(category);
        } else {
            tasks = taskService.getAllTasksForCurrentUser();
        }

        return ResponseEntity.ok(
            ApiResponse.success("Tasks retrieved successfully", tasks)
        );
    }

    /**
     * GET /api/tasks/dashboard - Get dashboard statistics.
     * Returns counts, rates, and chart data for the dashboard.
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats() {
        DashboardStats stats = taskService.getDashboardStats();
        return ResponseEntity.ok(
            ApiResponse.success("Dashboard stats retrieved", stats)
        );
    }

    /**
     * GET /api/tasks/{id} - Get a specific task by ID.
     *
     * @PathVariable extracts the {id} from the URL path
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
        @PathVariable Long id) {

        TaskResponse task = taskService.getTaskById(id);
        return ResponseEntity.ok(
            ApiResponse.success("Task retrieved successfully", task)
        );
    }

    // ==========================================
    // UPDATE
    // ==========================================

    /**
     * PUT /api/tasks/{id} - Update a task.
     * All provided fields will be updated (partial update - null fields are ignored).
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a task")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
        @PathVariable Long id,
        @Valid @RequestBody UpdateTaskRequest request) {

        TaskResponse task = taskService.updateTask(id, request);
        return ResponseEntity.ok(
            ApiResponse.success("Task updated successfully", task)
        );
    }

    /**
     * PATCH /api/tasks/{id}/complete - Mark a task as completed.
     * Convenience endpoint for the "Complete" button in the UI.
     *
     * PATCH is used for partial updates - we're only changing the status.
     */
    @PatchMapping("/{id}/complete")
    @Operation(summary = "Mark task as completed")
    public ResponseEntity<ApiResponse<TaskResponse>> markTaskCompleted(
        @PathVariable Long id) {

        TaskResponse task = taskService.markTaskCompleted(id);
        return ResponseEntity.ok(
            ApiResponse.success("Task marked as completed!", task)
        );
    }

    // ==========================================
    // DELETE
    // ==========================================

    /**
     * DELETE /api/tasks/{id} - Delete a task permanently.
     *
     * Returns 204 No Content (successful deletion with no response body)
     * This is a REST convention for DELETE operations.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
        @PathVariable Long id) {

        taskService.deleteTask(id);
        return ResponseEntity.ok(
            ApiResponse.success("Task deleted successfully", null)
        );
    }
}
