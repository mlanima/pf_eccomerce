package com.horseriding.ecommerce.users.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user registration requests.
 * Contains validation annotations to ensure data integrity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class UserRegistrationRequest {

    /** Minimum password length constant. */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /** User's email address for registration. */
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    /** User's password for registration. */
    @NotBlank(message = "Password is required")
    @Size(min = MIN_PASSWORD_LENGTH, message = "Password must be at least 8 characters long")
    private String password;

    /** User's first name. */
    @NotBlank(message = "First name is required")
    private String firstName;

    /** User's last name. */
    @NotBlank(message = "Last name is required")
    private String lastName;

    /** User's phone number (optional). */
    private String phoneNumber;
}
