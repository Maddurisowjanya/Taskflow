package com.taskflow.repository;

import com.taskflow.entity.Role;
import com.taskflow.entity.Role.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * RoleRepository - Data Access Layer for Role entity.
 * Used to look up roles by name when assigning roles to users.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by its ERole enum name.
     * Used during user registration to assign the default ROLE_USER.
     *
     * @param name the ERole enum value (e.g., ERole.ROLE_USER)
     * @return Optional<Role>
     */
    Optional<Role> findByName(ERole name);
}
