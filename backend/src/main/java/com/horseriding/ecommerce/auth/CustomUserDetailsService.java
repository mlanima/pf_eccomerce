package com.horseriding.ecommerce.auth;

import com.horseriding.ecommerce.exception.ResourceNotFoundException;
import com.horseriding.ecommerce.users.User;
import com.horseriding.ecommerce.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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
    private final AccountStatusUserDetailsChecker detailsChecker =
            new AccountStatusUserDetailsChecker();

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
            User user =
                    userRepository
                            .findByEmail(username)
                            .orElseThrow(
                                    () ->
                                            new UsernameNotFoundException(
                                                    "User not found with email: " + username));

            UserPrincipal userPrincipal = UserPrincipal.create(user);

            // Check account status (locked, disabled, expired)
            validateUserStatus(userPrincipal);

            log.debug("Successfully loaded user details for: {}", username);
            return userPrincipal;
        } catch (UsernameNotFoundException e) {
            log.warn("User not found with email: {}", username);
            throw e;
        } catch (DisabledException | LockedException e) {
            log.warn("Account validation failed for user {}: {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to load user details for username: {}", username, e);
            throw new UsernameNotFoundException("Authentication failed for user: " + username, e);
        }
    }

    /**
     * Loads user details by user ID for Spring Security authentication.
     *
     * @param id the user ID to load
     * @return UserDetails implementation containing user information
     * @throws UsernameNotFoundException if user is not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        log.debug("Loading user details for ID: {}", id);

        try {
            User user =
                    userRepository
                            .findById(id)
                            .orElseThrow(
                                    () ->
                                            new UsernameNotFoundException(
                                                    "User not found with ID: " + id));

            UserPrincipal userPrincipal = UserPrincipal.create(user);

            // Check account status (locked, disabled, expired)
            validateUserStatus(userPrincipal);

            log.debug("Successfully loaded user details for ID: {}", id);
            return userPrincipal;
        } catch (UsernameNotFoundException e) {
            log.warn("User not found with ID: {}", id);
            throw e;
        } catch (DisabledException | LockedException e) {
            log.warn("Account validation failed for user ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to load user details for ID: {}", id, e);
            throw new UsernameNotFoundException("Authentication failed for user ID: " + id, e);
        }
    }

    /**
     * Gets a user by email without creating a UserPrincipal.
     * Used for internal service operations.
     *
     * @param email the email to look up
     * @return the User entity
     * @throws ResourceNotFoundException if user is not found
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) throws ResourceNotFoundException {
        log.debug("Getting user by email: {}", email);

        try {
            return userRepository
                    .findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        } catch (ResourceNotFoundException e) {
            log.warn("User not found with email: {}", email);
            throw e;
        } catch (Exception e) {
            log.error("Error getting user by email: {}", email, e);
            throw new ResourceNotFoundException("User", "email", email);
        }
    }

    /**
     * Gets a user by ID without creating a UserPrincipal.
     * Used for internal service operations.
     *
     * @param id the user ID to look up
     * @return the User entity
     * @throws ResourceNotFoundException if user is not found
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) throws ResourceNotFoundException {
        log.debug("Getting user by ID: {}", id);

        try {
            return userRepository
                    .findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        } catch (ResourceNotFoundException e) {
            log.warn("User not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error getting user by ID: {}", id, e);
            throw new ResourceNotFoundException("User", "id", id);
        }
    }

    /**
     * Validates the user's account status.
     * Checks if the account is locked, disabled, or expired.
     *
     * @param userDetails the user details to validate
     * @throws DisabledException if the account is disabled
     * @throws LockedException if the account is locked
     */
    private void validateUserStatus(UserDetails userDetails) {
        detailsChecker.check(userDetails);
    }
}
