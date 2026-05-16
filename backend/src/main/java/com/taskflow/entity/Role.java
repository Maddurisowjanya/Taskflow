package com.taskflow.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Role Entity - Represents a user role (e.g., ROLE_USER, ROLE_ADMIN).
 *
 * Spring Security requires roles to be prefixed with "ROLE_"
 * so we store them as "ROLE_USER" and "ROLE_ADMIN" in the DB.
 */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Role name - using an Enum to ensure only valid values are stored.
     *
     * @Enumerated(EnumType.STRING): stores enum as "ROLE_USER" string in DB
     * (not as number like 0, 1 which would be EnumType.ORDINAL)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private ERole name;

    /**
     * ERole - Enum of all possible roles in the system.
     * Defined as inner enum for simplicity.
     */
    public enum ERole {
        ROLE_USER,   // Regular user - can manage own tasks
        ROLE_ADMIN   // Admin user - can manage all tasks and users
    }
}
