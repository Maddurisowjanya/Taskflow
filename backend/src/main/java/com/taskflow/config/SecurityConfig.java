package com.taskflow.config;

import com.taskflow.security.AuthTokenFilter;
import com.taskflow.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * SecurityConfig - Spring Security Configuration.
 *
 * This is the heart of our security setup. It configures:
 * 1. Which endpoints require authentication
 * 2. JWT filter chain
 * 3. CORS (Cross-Origin Resource Sharing) for React frontend
 * 4. Password encoding
 * 5. Session management (STATELESS for JWT)
 *
 * @Configuration: marks as configuration class
 * @EnableWebSecurity: enables Spring Security
 * @EnableMethodSecurity: enables @PreAuthorize annotations on methods
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Register our JWT filter as a Spring Bean.
     * @Bean tells Spring to manage this object.
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * BCryptPasswordEncoder - Hashes passwords using BCrypt algorithm.
     *
     * BCrypt automatically:
     * - Adds a random salt to prevent rainbow table attacks
     * - Applies multiple rounds of hashing (slow = secure)
     * - Never produces the same hash twice for same password
     *
     * Strength 10 = 2^10 = 1024 iterations (good balance of security/performance)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * DaoAuthenticationProvider - Authenticates users using database.
     * Connects our UserDetailsService (loads user from DB) with the PasswordEncoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AuthenticationManager - Used in AuthController to authenticate login requests.
     * Spring Boot auto-configures this from our AuthenticationProvider.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
        throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * SecurityFilterChain - Main security configuration.
     *
     * Defines rules for:
     * - Which URLs are public vs protected
     * - Session management strategy
     * - CORS configuration
     * - JWT filter placement
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF - not needed for stateless JWT APIs
            // (CSRF protects session-based apps, not JWT-based ones)
            .csrf(csrf -> csrf.disable())

            // Configure CORS to allow React frontend (localhost:3000)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Configure exception handling for unauthenticated requests
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(401, "Error: Unauthorized - " + authException.getMessage());
                })
            )

            // Use STATELESS sessions - no cookies, no session storage
            // Every request must include the JWT token
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Define URL authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - anyone can access without a token
                .requestMatchers("/api/auth/**").permitAll()          // Login/Register
                .requestMatchers("/swagger-ui/**").permitAll()        // Swagger UI
                .requestMatchers("/swagger-ui.html").permitAll()      // Swagger HTML
                .requestMatchers("/api-docs/**").permitAll()          // OpenAPI docs
                .requestMatchers("/v3/api-docs/**").permitAll()       // OpenAPI docs v3

                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // All other endpoints require authentication
                .anyRequest().authenticated()
            );

        // Register our custom authentication provider
        http.authenticationProvider(authenticationProvider());

        // Add JWT filter BEFORE the default UsernamePasswordAuthenticationFilter
        // This ensures JWT validation happens before Spring Security's default auth
        http.addFilterBefore(
            authenticationJwtTokenFilter(),
            UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    /**
     * CORS Configuration - Allows the React frontend to call our API.
     *
     * CORS (Cross-Origin Resource Sharing) is a browser security feature that
     * blocks requests from different origins (different domain/port).
     * We need to explicitly allow our React app (localhost:3000) to call our API.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from React dev server and production domain
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",  // React development server
            "http://localhost:5173",  // Vite development server
            "https://yourdomain.com"  // Production domain (update this)
        ));

        // Allow all common HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow all headers (including Authorization with JWT)
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache CORS preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
