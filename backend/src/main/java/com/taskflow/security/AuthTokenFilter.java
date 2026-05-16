package com.taskflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * AuthTokenFilter - JWT Authentication Filter.
 *
 * This filter runs ONCE per HTTP request (extends OncePerRequestFilter).
 * It intercepts every request and:
 * 1. Extracts the JWT token from the Authorization header
 * 2. Validates the token
 * 3. Loads user details from the database
 * 4. Sets the authentication in Spring Security's SecurityContext
 *
 * After this filter runs, Spring Security knows who the current user is.
 *
 * Request flow:
 * HTTP Request → AuthTokenFilter → Controller → Service → Repository
 */
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Main filter method called for every HTTP request.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain passes request to the next filter in chain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        try {
            // Step 1: Extract JWT from Authorization header
            String jwt = parseJwt(request);

            // Step 2: Validate the JWT if present
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

                // Step 3: Get username from JWT payload
                String username = jwtUtils.getUsernameFromJwtToken(jwt);

                // Step 4: Load full user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Step 5: Create authentication token with user details and authorities
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,                           // No credentials needed (JWT already verified)
                        userDetails.getAuthorities()    // User's roles/permissions
                    );

                // Add request details (IP address, session) to authentication
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Step 6: Store authentication in SecurityContext
                // This tells Spring Security "this user is authenticated"
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        // Continue to the next filter / controller
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from the Authorization header.
     *
     * Expected header format: "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
     *
     * @param request the HTTP request
     * @return the JWT string, or null if not present
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        // Check if header exists and starts with "Bearer "
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Remove "Bearer " prefix (7 characters)
        }

        return null;
    }
}
