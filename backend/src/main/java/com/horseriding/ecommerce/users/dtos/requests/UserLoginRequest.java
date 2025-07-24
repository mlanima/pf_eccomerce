package com.horseriding.ecommerce.users.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user login requests.
 * Contains email and password validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class UserLoginRequest {

    /** User's email address for authentication. */
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    /** User's password for authentication. */
    @NotBlank(message = "Password is required")
    private String password;
}
