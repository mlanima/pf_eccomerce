package com.horseriding.ecommerce.auth;

import com.horseriding.ecommerce.users.User;
import com.horseriding.ecommerce.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation for Spring Security integration.
 * Loads user details from the database for authentication and authorization.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user details by username (email) for Spring Security authentication.
     *
     * @param username the username (email) to load
     * @return UserDetails implementation containing user information
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);
        
        try {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
            
            log.debug("Successfully loaded user details for: {}", username);
            return UserPrincipal.create(user);
        } catch (Exception e) {
            log.error("Failed to load user details for username: {}", username, e);
            throw new UsernameNotFoundException("Authentication failed for user: " + username, e);
        }
    }
}