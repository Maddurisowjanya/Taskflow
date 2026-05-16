package com.taskflow.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JwtUtils - Utility class for JWT token operations.
 *
 * Handles:
 * 1. Generating JWT tokens after successful login
 * 2. Validating JWT tokens from incoming requests
 * 3. Extracting username from JWT tokens
 *
 * JWT Structure: header.payload.signature
 * - Header: algorithm type (HS256)
 * - Payload: claims (username, expiration, issued at)
 * - Signature: HMAC-SHA256 signed with secret key
 */
@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    /**
     * JWT secret key - injected from application.properties
     * Used to sign and verify tokens.
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * JWT expiration time in milliseconds - injected from application.properties
     */
    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Generate a JWT token for an authenticated user.
     *
     * @param authentication the Authentication object after login
     * @return JWT token string
     */
    public String generateJwtToken(Authentication authentication) {
        // Get the UserDetails object from the authentication
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
            .setSubject(userPrincipal.getUsername())           // Username in payload
            .setIssuedAt(new Date())                           // Token creation time
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // Expiry
            .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Sign with secret
            .compact();
    }

    /**
     * Generate a JWT token directly from a username string.
     * Used for token refresh scenarios.
     *
     * @param username the username to include in the token
     * @return JWT token string
     */
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Extract the username (subject) from a JWT token.
     *
     * @param token the JWT token string
     * @return username stored in the token
     */
    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())  // Verify signature with same key
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();                   // Get username from payload
    }

    /**
     * Validate a JWT token.
     * Checks: signature validity, expiration, and format.
     *
     * @param authToken the JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
            return true;

        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Build the signing Key from the secret string.
     * HMAC-SHA256 requires a key of at least 256 bits (32 bytes).
     *
     * @return Key object for signing/verifying JWTs
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
