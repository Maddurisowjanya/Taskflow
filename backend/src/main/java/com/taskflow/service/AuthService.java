package com.taskflow.service;

import com.taskflow.dto.TaskFlowDTOs.*;
import com.taskflow.entity.Role;
import com.taskflow.entity.Role.ERole;
import com.taskflow.entity.User;
import com.taskflow.repository.RoleRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtUtils;
import com.taskflow.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AuthService - Business logic for authentication operations.
 *
 * Service layer sits between Controller and Repository.
 * Controllers call Services, Services call Repositories.
 *
 * Handles:
 * - User registration with password hashing
 * - User login with JWT generation
 */
@Service
@Transactional  // All methods run in a database transaction
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Register a new user.
     *
     * Steps:
     * 1. Check if username/email already exists
     * 2. Hash the password (NEVER store plain text!)
     * 3. Assign default ROLE_USER
     * 4. Save to database
     *
     * @param request registration data from the request body
     * @return success/failure message
     */
    public String register(RegisterRequest request) {
        // Step 1: Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username '" + request.getUsername() + "' is already taken!");
        }

        // Step 1b: Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email '" + request.getEmail() + "' is already registered!");
        }

        // Step 2: Create new user with HASHED password
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword())) // BCrypt hash!
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .isActive(true)
            .build();

        // Step 3: Assign default ROLE_USER
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
            .orElseThrow(() -> new RuntimeException(
                "Error: ROLE_USER not found in database. Please run the schema.sql seed data."));
        roles.add(userRole);
        user.setRoles(roles);

        // Step 4: Save to database
        userRepository.save(user);

        return "User registered successfully! You can now log in.";
    }

    /**
     * Authenticate user and generate JWT token.
     *
     * Steps:
     * 1. Authenticate credentials against database
     * 2. Set authentication in security context
     * 3. Generate JWT token
     * 4. Build and return response with token + user details
     *
     * @param request login credentials from request body
     * @return JWT token and user details
     */
    public JwtResponse login(LoginRequest request) {
        // Step 1: Authenticate - Spring Security checks username/password against DB
        // This internally calls UserDetailsServiceImpl.loadUserByUsername()
        // and compares the hashed passwords.
        // Throws BadCredentialsException if credentials are wrong.
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        // Step 2: Set authentication in security context
        // This tells Spring Security the current user is authenticated
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Step 3: Generate JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Step 4: Extract user details from authentication object
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Extract role names from authorities
        List<String> roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        // Get full user for additional details
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Build and return the JWT response
        return JwtResponse.builder()
            .token(jwt)
            .type("Bearer")
            .id(userDetails.getId())
            .username(userDetails.getUsername())
            .email(userDetails.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .roles(roles)
            .build();
    }
}
