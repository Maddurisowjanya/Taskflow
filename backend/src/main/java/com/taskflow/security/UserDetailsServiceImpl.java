package com.taskflow.security;

import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserDetailsServiceImpl - Implementation of Spring Security's UserDetailsService.
 *
 * Spring Security calls loadUserByUsername() during authentication
 * to get user details for verification.
 *
 * @Service marks this as a Spring service component
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user details by username for Spring Security authentication.
     *
     * This method is called automatically by Spring Security when a user
     * tries to log in. It fetches the user from DB and wraps them in
     * UserDetailsImpl for authentication processing.
     *
     * @Transactional ensures the roles collection is loaded within the transaction
     * (needed because of LAZY loading on the roles relationship)
     *
     * @param username the username from the login request
     * @return UserDetails implementation
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user by username OR email (flexible login)
        User user = userRepository.findByUsername(username)
            .orElseGet(() -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with username or email: " + username
                ))
            );

        // Convert User entity to UserDetailsImpl
        return UserDetailsImpl.build(user);
    }
}
