package com.taskflow.exception;

import com.taskflow.dto.TaskFlowDTOs.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// ==========================================
// Custom Exception Classes
// ==========================================

/**
 * ResourceNotFoundException - Thrown when a requested resource doesn't exist.
 * Example: GET /api/tasks/999 → task 999 doesn't exist
 * Results in HTTP 404 Not Found
 */
class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

/**
 * ResourceAlreadyExistsException - Thrown when trying to create a duplicate.
 * Example: Register with an email that's already taken
 * Results in HTTP 409 Conflict
 */
class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}

/**
 * UnauthorizedException - Thrown when a user tries to access someone else's resource.
 * Example: User A tries to delete User B's task
 * Results in HTTP 403 Forbidden
 */
class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

/**
 * BadRequestException - Thrown for invalid input that passes validation.
 * Example: Current password doesn't match when changing password
 * Results in HTTP 400 Bad Request
 */
class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

// ==========================================
// Global Exception Handler
// ==========================================

/**
 * GlobalExceptionHandler - Centralized exception handling for all controllers.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * Intercepts exceptions thrown anywhere in the application
 * and converts them to proper HTTP responses.
 *
 * Without this, Spring would return a generic error page or stack trace.
 * With this, we return clean, consistent JSON error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle resource not found (404).
     * Example: Task ID doesn't exist in database.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
        ResourceNotFoundException ex) {
        logger.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle duplicate resource creation (409).
     * Example: Username or email already taken.
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceAlreadyExistsException(
        ResourceAlreadyExistsException ex) {
        logger.error("Resource already exists: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle unauthorized access (403).
     * Example: User tries to modify another user's task.
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(
        UnauthorizedException ex) {
        logger.error("Unauthorized access: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle bad request (400).
     * Example: Wrong current password when changing password.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(
        BadRequestException ex) {
        logger.error("Bad request: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handle Spring validation errors (400).
     * Triggered by @Valid annotation when request body fails validation.
     *
     * Returns a map of field names → error messages.
     * Example: {"email": "must be a valid email", "username": "size must be 3-50"}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
        MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // Collect all field-level validation errors
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.error("Validation failed: {}", errors);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .build()
            );
    }

    /**
     * Handle wrong credentials (401).
     * Triggered when username/password don't match.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(
        BadCredentialsException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Invalid username or password"));
    }

    /**
     * Handle Spring Security's access denied (403).
     * Triggered by @PreAuthorize when user lacks the required role.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
        AccessDeniedException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("Access denied: You don't have permission to perform this action"));
    }

    /**
     * Handle all other unexpected exceptions (500).
     * Catch-all for any exception not handled above.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllUncaughtException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An unexpected error occurred. Please try again later."));
    }
}
