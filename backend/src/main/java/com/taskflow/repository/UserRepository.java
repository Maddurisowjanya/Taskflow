package com.taskflow.repository;

import com.taskflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository - Data Access Layer for User entity.
 *
 * Extends JpaRepository which provides built-in CRUD methods:
 * - save(), findById(), findAll(), deleteById(), count(), etc.
 *
 * Spring Data JPA auto-implements this interface at runtime.
 * No need to write SQL for common operations!
 *
 * Method naming convention: Spring generates queries from method names
 * e.g., findByEmail → SELECT * FROM users WHERE email = ?
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their username.
     * Used during login to load user details.
     *
     * @param username the username to search for
     * @return Optional<User> - empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their email address.
     * Used for email-based lookups.
     *
     * @param email the email to search for
     * @return Optional<User> - empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a username is already taken.
     * Used during registration to prevent duplicates.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    Boolean existsByUsername(String username);

    /**
     * Check if an email is already registered.
     * Used during registration to prevent duplicates.
     *
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    Boolean existsByEmail(String email);

    /**
     * Find user by username OR email (for flexible login).
     * Custom JPQL query - JPQL uses entity class names, not table names.
     *
     * @param username the username
     * @param email the email
     * @return Optional<User>
     */
    @Query("SELECT u FROM User u WHERE u.username = :username OR u.email = :email")
    Optional<User> findByUsernameOrEmail(String username, String email);
}
