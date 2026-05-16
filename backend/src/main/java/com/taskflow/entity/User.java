package com.taskflow.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity - Represents a user in the database.
 *
 * @Entity tells JPA/Hibernate this class maps to a database table.
 * @Table(name = "users") specifies the table name.
 * Lombok annotations (@Data, @Builder, etc.) auto-generate boilerplate code.
 */
@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),  // Username must be unique
        @UniqueConstraint(columnNames = "email")      // Email must be unique
    }
)
@Data                   // Generates getters, setters, toString, equals, hashCode
@Builder                // Enables builder pattern: User.builder().username("x").build()
@NoArgsConstructor      // Generates no-argument constructor (required by JPA)
@AllArgsConstructor     // Generates constructor with all fields
public class User {

    /** Primary key - auto-incremented by the database */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username: required, 3-50 characters */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Email: required, valid email format */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** Password: stored as BCrypt hash (never plain text!) */
    @NotBlank(message = "Password is required")
    @Column(nullable = false, length = 255)
    private String password;

    /** Optional profile fields */
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    /** Account status - can be deactivated by admin */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Many-to-Many relationship with Role.
     * A user can have multiple roles, a role can belong to multiple users.
     *
     * @ManyToMany: defines the relationship type
     * @JoinTable: specifies the join table 'user_roles'
     * FetchType.LAZY: roles are loaded only when accessed (better performance)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * One-to-Many relationship with Task.
     * One user can have many tasks.
     * mappedBy = "user": Task entity owns this relationship (has the foreign key)
     * cascade = ALL: operations on User cascade to Tasks (e.g., delete user → delete tasks)
     * orphanRemoval = true: tasks without a user are automatically deleted
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude   // Avoid infinite recursion in toString()
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();

    /** Auto-set when the record is created */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Auto-updated whenever the record changes */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
