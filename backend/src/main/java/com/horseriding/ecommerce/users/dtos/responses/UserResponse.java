package com.horseriding.ecommerce.users.dtos.responses;

import com.horseriding.ecommerce.users.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for admin user management responses.
 * Used by administrators to view user information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Utility method to get full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}