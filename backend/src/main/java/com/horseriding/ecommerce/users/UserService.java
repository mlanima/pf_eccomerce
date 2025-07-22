package com.horseriding.ecommerce.users;

import com.horseriding.ecommerce.auth.TokenService;
import com.horseriding.ecommerce.auth.dtos.responses.AuthResponse;
import com.horseriding.ecommerce.exception.AccessDeniedException;
import com.horseriding.ecommerce.exception.AuthenticationException;
import com.horseriding.ecommerce.exception.ResourceNotFoundException;
import com.horseriding.ecommerce.users.dtos.requests.UserLoginRequest;
import com.horseriding.ecommerce.users.dtos.requests.UserRegistrationRequest;
import com.horseriding.ecommerce.users.dtos.requests.UserUpdateRequest;
import com.horseriding.ecommerce.users.dtos.responses.UserProfileResponse;
import com.horseriding.ecommerce.users.dtos.responses.UserResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user management operations.
 * Handles user registration, authentication, and profile management.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    /** Repository for user data access. */
    private final UserRepository userRepository;

    /** Password encoder for secure password storage. */
    private final PasswordEncoder passwordEncoder;

    /** Token service for JWT token management. */
    private final TokenService tokenService;

    /**
     * Registers a new user with CUSTOMER role.
     * No email verification is required.
     *
     * @param request the user registration request
     * @return the created user profile
     * @throws IllegalArgumentException if email is already in use
     */
    @Transactional
    public UserProfileResponse registerUser(final UserRegistrationRequest request) {
        // Check if email is already in use
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        // Create new user with CUSTOMER role
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName(),
                UserRole.CUSTOMER);

        user.setPhoneNumber(request.getPhoneNumber());

        // Save user to database
        User savedUser = userRepository.save(user);

        // Return user profile
        return mapToUserProfileResponse(savedUser);
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param request the login request
     * @return authentication response with JWT token and user profile
     * @throws AuthenticationException if authentication fails
     */
    public AuthResponse loginUser(final UserLoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Generate JWT tokens (access and refresh)
        TokenService.TokenPair tokens = tokenService.createTokens(user);

        // Return authentication response with both tokens
        return new AuthResponse(
                tokens.getAccessToken(),
                tokens.getRefreshToken(),
                mapToUserProfileResponse(user),
                user.getRole(),
                900 // 15 minutes in seconds (access token expiration)
        );
    }

    /**
     * Gets a user profile by ID.
     *
     * @param userId the user ID
     * @return the user profile
     * @throws ResourceNotFoundException if user is not found
     */
    public UserProfileResponse getUserProfile(final Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToUserProfileResponse(user);
    }

    /**
     * Updates a user profile.
     *
     * @param userId the user ID
     * @param request the update request
     * @return the updated user profile
     * @throws ResourceNotFoundException if user is not found
     */
    @Transactional
    public UserProfileResponse updateUserProfile(final Long userId, final UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update user fields
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());

        // Save updated user
        User updatedUser = userRepository.save(user);

        return mapToUserProfileResponse(updatedUser);
    }

    /**
     * Creates a new admin user (superadmin only).
     *
     * @param currentUserId the ID of the user making the request
     * @param request the user registration request
     * @return the created admin user
     * @throws AccessDeniedException if the current user is not a superadmin
     */
    @Transactional
    public UserResponse createAdminUser(final Long currentUserId, final UserRegistrationRequest request) {
        // Check if current user is a superadmin
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!currentUser.isSuperAdmin()) {
            throw new AccessDeniedException("Only superadmins can create admin users");
        }

        // Check if email is already in use
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        // Create new user with ADMIN role
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                request.getLastName(),
                UserRole.ADMIN);

        user.setPhoneNumber(request.getPhoneNumber());

        // Save user to database
        User savedUser = userRepository.save(user);

        // Return user response
        return mapToUserResponse(savedUser);
    }

    /**
     * Gets all admin users (superadmin only).
     *
     * @param currentUserId the ID of the user making the request
     * @return list of admin users
     * @throws AccessDeniedException if the current user is not a superadmin
     */
    public List<UserResponse> getAllAdminUsers(final Long currentUserId) {
        // Check if current user is a superadmin
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!currentUser.isSuperAdmin()) {
            throw new AccessDeniedException("Only superadmins can view admin users");
        }

        // Get all admin users
        List<User> adminUsers = userRepository.findAll().stream()
                .filter(user -> user.isAdmin() || user.isSuperAdmin())
                .collect(Collectors.toList());

        // Map to response DTOs
        return adminUsers.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deletes an admin user (superadmin only).
     *
     * @param currentUserId the ID of the user making the request
     * @param adminUserId the ID of the admin user to delete
     * @throws AccessDeniedException if the current user is not a superadmin
     * @throws IllegalArgumentException if trying to delete a superadmin
     */
    @Transactional
    public void deleteAdminUser(final Long currentUserId, final Long adminUserId) {
        // Check if current user is a superadmin
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!currentUser.isSuperAdmin()) {
            throw new AccessDeniedException("Only superadmins can delete admin users");
        }

        // Get admin user to delete
        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        // Check if admin user is a superadmin
        if (adminUser.isSuperAdmin()) {
            throw new IllegalArgumentException("Cannot delete superadmin users");
        }

        // Check if admin user is actually an admin
        if (!adminUser.isAdmin()) {
            throw new IllegalArgumentException("User is not an admin");
        }

        // Delete admin user
        userRepository.delete(adminUser);
    }

    /**
     * Updates an admin user's role (superadmin only).
     *
     * @param currentUserId the ID of the user making the request
     * @param adminUserId the ID of the admin user to update
     * @param newRole the new role for the admin user
     * @return the updated admin user
     * @throws AccessDeniedException if the current user is not a superadmin
     * @throws IllegalArgumentException if trying to update a superadmin or set an invalid role
     */
    @Transactional
    public UserResponse updateAdminUserRole(
            final Long currentUserId, final Long adminUserId, final UserRole newRole) {
        // Check if current user is a superadmin
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!currentUser.isSuperAdmin()) {
            throw new AccessDeniedException("Only superadmins can update admin roles");
        }

        // Get admin user to update
        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));

        // Check if admin user is a superadmin
        if (adminUser.isSuperAdmin() && !adminUser.getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Cannot update other superadmin users");
        }

        // Check if new role is CUSTOMER
        if (newRole == UserRole.CUSTOMER) {
            throw new IllegalArgumentException("Cannot downgrade admin to customer");
        }

        // Update admin user role
        adminUser.setRole(newRole);
        User updatedUser = userRepository.save(adminUser);

        // Return updated user
        return mapToUserResponse(updatedUser);
    }

    /**
     * Searches for users with pagination (admin only).
     *
     * @param currentUserId the ID of the user making the request
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return page of users matching the search criteria
     * @throws AccessDeniedException if the current user is not an admin or superadmin
     */
    public Page<UserResponse> searchUsers(
            final Long currentUserId, final String searchTerm, final Pageable pageable) {
        // Check if current user is an admin or superadmin
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!currentUser.hasAdminPrivileges()) {
            throw new AccessDeniedException("Only admins can search users");
        }

        // Search users
        Page<User> users = userRepository.searchUsers(searchTerm, pageable);

        // Map to response DTOs
        return users.map(this::mapToUserResponse);
    }

    /**
     * Maps a User entity to a UserProfileResponse DTO.
     *
     * @param user the user entity
     * @return the user profile response DTO
     */
    private UserProfileResponse mapToUserProfileResponse(final User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getRole(),
                true, // Active status (not implemented in entity yet)
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    /**
     * Maps a User entity to a UserResponse DTO.
     *
     * @param user the user entity
     * @return the user response DTO
     */
    private UserResponse mapToUserResponse(final User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}