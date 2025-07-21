package com.horseriding.ecommerce.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity with role-based queries.
 * Provides methods for finding users by role, status, and other criteria.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email.
     * Used for authentication and user lookup.
     *
     * @param email The email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all users with a specific role.
     * Used for admin management and role-based operations.
     *
     * @param role The role to filter by
     * @return List of users with the specified role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find all users with a specific role with pagination.
     * Used for admin management interfaces.
     *
     * @param role The role to filter by
     * @param pageable Pagination information
     * @return Page of users with the specified role
     */
    Page<User> findByRole(UserRole role, Pageable pageable);

    /**
     * Find all active users with a specific role.
     * Used for filtering active administrators.
     *
     * @param role The role to filter by
     * @param active The active status to filter by
     * @return List of active users with the specified role
     */
    List<User> findByRoleAndActive(UserRole role, boolean active);

    /**
     * Find all active users with a specific role with pagination.
     * Used for admin management interfaces with active status filtering.
     *
     * @param role The role to filter by
     * @param active The active status to filter by
     * @param pageable Pagination information
     * @return Page of active users with the specified role
     */
    Page<User> findByRoleAndActive(UserRole role, boolean active, Pageable pageable);

    /**
     * Check if a user with the given email exists.
     * Used for registration validation.
     *
     * @param email The email to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find all users with admin privileges (ADMIN or SUPERADMIN roles).
     * Used for system administration tasks.
     *
     * @return List of users with admin privileges
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' OR u.role = 'SUPERADMIN'")
    List<User> findAllAdmins();

    /**
     * Find all users with admin privileges (ADMIN or SUPERADMIN roles) with pagination.
     * Used for system administration interfaces.
     *
     * @param pageable Pagination information
     * @return Page of users with admin privileges
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' OR u.role = 'SUPERADMIN'")
    Page<User> findAllAdmins(Pageable pageable);

    /**
     * Find all active users with admin privileges (ADMIN or SUPERADMIN roles).
     * Used for system administration tasks with active status filtering.
     *
     * @param active The active status to filter by
     * @return List of active users with admin privileges
     */
    @Query("SELECT u FROM User u WHERE (u.role = 'ADMIN' OR u.role = 'SUPERADMIN') AND u.active = :active")
    List<User> findAllAdminsByActive(@Param("active") boolean active);

    /**
     * Find all active users with admin privileges (ADMIN or SUPERADMIN roles) with pagination.
     * Used for system administration interfaces with active status filtering.
     *
     * @param active The active status to filter by
     * @param pageable Pagination information
     * @return Page of active users with admin privileges
     */
    @Query("SELECT u FROM User u WHERE (u.role = 'ADMIN' OR u.role = 'SUPERADMIN') AND u.active = :active")
    Page<User> findAllAdminsByActive(@Param("active") boolean active, Pageable pageable);

    /**
     * Search for users by name or email with pagination.
     * Used for admin user management with search functionality.
     *
     * @param searchTerm The search term to match against name or email
     * @param pageable Pagination information
     * @return Page of users matching the search criteria
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search for users by name or email with role filtering and pagination.
     * Used for admin user management with search and role filtering.
     *
     * @param searchTerm The search term to match against name or email
     * @param role The role to filter by
     * @param pageable Pagination information
     * @return Page of users matching the search criteria and role
     */
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "u.role = :role")
    Page<User> searchUsersByRole(@Param("searchTerm") String searchTerm, @Param("role") UserRole role, Pageable pageable);
}