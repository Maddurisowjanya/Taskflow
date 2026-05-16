package com.taskflow.controller;

import com.taskflow.dto.TaskFlowDTOs.*;
import com.taskflow.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - REST API endpoints for authentication.
 *
 * Base URL: /api/auth
 * Public endpoints (no JWT required) - configured in SecurityConfig
 *
 * @RestController = @Controller + @ResponseBody
 * (automatically serializes return values to JSON)
 *
 * @RequestMapping sets the base URL prefix for all methods in this class
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)  // Allow cross-origin requests
@Tag(name = "Authentication", description = "User registration and login APIs")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * POST /api/auth/register - Register a new user account.
     *
     * Request body example:
     * {
     *   "username": "johndoe",
     *   "email": "john@example.com",
     *   "password": "secret123",
     *   "firstName": "John",
     *   "lastName": "Doe"
     * }
     *
     * @Valid triggers Bean Validation on the request body
     * @RequestBody deserializes JSON request body to RegisterRequest
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with ROLE_USER")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<ApiResponse<String>> register(
        @Valid @RequestBody RegisterRequest request) {

        String message = authService.register(request);

        // Return 201 Created with success message
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(message, null));
    }

    /**
     * POST /api/auth/login - Authenticate user and get JWT token.
     *
     * Request body example:
     * {
     *   "username": "johndoe",
     *   "password": "secret123"
     * }
     *
     * Response example:
     * {
     *   "success": true,
     *   "message": "Login successful",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiJ9...",
     *     "type": "Bearer",
     *     "id": 1,
     *     "username": "johndoe",
     *     "email": "john@example.com",
     *     "roles": ["ROLE_USER"]
     *   }
     * }
     *
     * The frontend stores this token and sends it with every subsequent request:
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user credentials and returns JWT token")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<ApiResponse<JwtResponse>> login(
        @Valid @RequestBody LoginRequest request) {

        JwtResponse jwtResponse = authService.login(request);

        return ResponseEntity.ok(
            ApiResponse.success("Login successful", jwtResponse)
        );
    }
}
