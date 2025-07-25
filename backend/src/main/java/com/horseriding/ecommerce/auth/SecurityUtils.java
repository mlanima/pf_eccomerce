package com.horseriding.ecommerce.auth;

import com.horseriding.ecommerce.users.User;
import com.horseriding.ecommerce.users.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Utility class for Spring Security operations.
 * Provides helper methods to get current authenticated user information.
 */
@Slf4j
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets the current authenticated user from Spring Security context.
     *
     * @return the current authenticated user
     * @throws IllegalStateException if no user is authenticated or user is not of expected type
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Attempted to get current user when no user is authenticated");
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUser();
        }

        // Fallback for backward compatibility during transition
        if (principal instanceof User) {
            return (User) principal;
        }

        if (principal instanceof UserDetails) {
            log.error(
                    "UserDetails principal is not of expected type UserPrincipal: {}",
                    principal.getClass().getName());
            throw new IllegalStateException(
                    "UserDetails principal is not of expected type UserPrincipal");
        }

        log.error("Unexpected principal type: {}", principal.getClass().getName());
        throw new IllegalStateException(
                "Unexpected principal type: " + principal.getClass().getName());
    }

    /**
     * Gets the current authenticated UserPrincipal from Spring Security context.
     *
     * @return the current authenticated UserPrincipal
     * @throws IllegalStateException if no user is authenticated or user is not of expected type
     */
    public static UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Attempted to get current UserPrincipal when no user is authenticated");
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }

        log.error("Principal is not of type UserPrincipal: {}", principal.getClass().getName());
        throw new IllegalStateException(
                "Principal is not of type UserPrincipal: " + principal.getClass().getName());
    }

    /**
     * Gets the current authenticated user's ID.
     *
     * @return the current user's ID
     * @throws IllegalStateException if no user is authenticated
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Gets the current authenticated user's email.
     *
     * @return the current user's email
     * @throws IllegalStateException if no user is authenticated
     */
    public static String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }

    /**
     * Gets the current authenticated user's role.
     *
     * @return the current user's role
     * @throws IllegalStateException if no user is authenticated
     */
    public static UserRole getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

    /**
     * Checks if there is a currently authenticated user.
     *
     * @return true if a user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Checks if the current user has the specified role.
     *
     * @param role the role to check (without ROLE_ prefix)
     * @return true if the user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        if (!isAuthenticated()) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Checks if the current user has admin privileges (ADMIN or SUPERADMIN role).
     *
     * @return true if the user has admin privileges, false otherwise
     */
    public static boolean hasAdminPrivileges() {
        return hasRole("ADMIN") || hasRole("SUPERADMIN");
    }

    /**
     * Checks if the current user has superadmin privileges.
     *
     * @return true if the user has superadmin privileges, false otherwise
     */
    public static boolean hasSuperAdminPrivileges() {
        return hasRole("SUPERADMIN");
    }

    /**
     * Checks if the current user is the owner of the specified resource.
     *
     * @param resourceUserId the user ID associated with the resource
     * @return true if the current user is the owner, false otherwise
     */
    public static boolean isResourceOwner(Long resourceUserId) {
        if (!isAuthenticated()) {
            return false;
        }

        try {
            Long currentUserId = getCurrentUserId();
            return currentUserId.equals(resourceUserId);
        } catch (Exception e) {
            log.error("Error checking resource ownership", e);
            return false;
        }
    }
}
