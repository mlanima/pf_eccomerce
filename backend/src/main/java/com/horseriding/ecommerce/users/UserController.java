package com.horseriding.ecommerce.users;

import com.horseriding.ecommerce.common.dtos.responses.PaginationResponse;
import com.horseriding.ecommerce.common.dtos.responses.SuccessResponse;
import com.horseriding.ecommerce.users.dtos.requests.UserRegistrationRequest;
import com.horseriding.ecommerce.users.dtos.requests.UserUpdateRequest;
import com.horseriding.ecommerce.users.dtos.responses.UserProfileResponse;
import com.horseriding.ecommerce.users.dtos.responses.UserResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for user management endpoints.
 * Handles user profile management and admin user management.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    /** User service for user management operations. */
    private final UserService userService;

    /**
     * Gets the current user's profile.
     *
     * @return the user profile
     */
    @GetMapping("/users/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        UserProfileResponse profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    /**
     * Updates the current user's profile.
     *
     * @param request the update request
     * @return the updated user profile
     */
    @PutMapping("/users/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        UserProfileResponse profile = userService.updateCurrentUserProfile(request);
        return ResponseEntity.ok(profile);
    }

    /**
     * Creates a new admin user (superadmin only).
     *
     * @param request the user registration request
     * @return the created admin user
     */
    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<UserResponse> createAdminUser(
            @Valid @RequestBody UserRegistrationRequest request) {
        UserResponse user = userService.createAdminUser(request);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    /**
     * Gets all admin users (superadmin only).
     *
     * @return list of admin users
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<UserResponse>> getAllAdminUsers() {
        List<UserResponse> users = userService.getAllAdminUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Deletes an admin user (superadmin only).
     *
     * @param adminUserId the ID of the admin user to delete
     * @return success response
     */
    @DeleteMapping("/admin/users/{adminUserId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<SuccessResponse<Void>> deleteAdminUser(@PathVariable Long adminUserId) {
        userService.deleteAdminUser(adminUserId);
        return ResponseEntity.ok(new SuccessResponse<>("Admin user deleted successfully", 200));
    }

    /**
     * Updates an admin user's role (superadmin only).
     *
     * @param adminUserId the ID of the admin user to update
     * @param role the new role for the admin user
     * @return the updated admin user
     */
    @PutMapping("/admin/users/{adminUserId}/role")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<UserResponse> updateAdminUserRole(
            @PathVariable Long adminUserId, @RequestParam UserRole role) {
        UserResponse user = userService.updateAdminUserRole(adminUserId, role);
        return ResponseEntity.ok(user);
    }

    /**
     * Searches for users with pagination (admin only).
     *
     * @param searchTerm the search term
     * @param page the page number (0-based)
     * @param size the page size
     * @param sortBy the field to sort by
     * @param sortDir the sort direction
     * @return page of users matching the search criteria
     */
    @GetMapping("/admin/users/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse<UserResponse>> searchUsers(
            @RequestParam(required = false, defaultValue = "") String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction direction =
                sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<UserResponse> usersPage = userService.searchUsers(searchTerm, pageable);

        PaginationResponse<UserResponse> response =
                new PaginationResponse<>(
                        usersPage.getContent(),
                        usersPage.getNumber(),
                        usersPage.getSize(),
                        usersPage.getTotalElements(),
                        usersPage.getTotalPages(),
                        usersPage.isFirst(),
                        usersPage.isLast());

        return ResponseEntity.ok(response);
    }
}
