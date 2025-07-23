package com.horseriding.ecommerce.auth.dtos.responses;

import com.horseriding.ecommerce.users.UserRole;
import com.horseriding.ecommerce.users.dtos.responses.UserProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication responses.
 * Contains access token, refresh token, and user information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** Access token for API authentication. */
    private String accessToken;

    /** Refresh token for obtaining new access tokens. */
    private String refreshToken;

    /** User profile information. */
    private UserProfileResponse user;

    /** User role for frontend authorization. */
    private UserRole role;

    /** Token type (always "Bearer"). */
    private String tokenType = "Bearer";

    /** Access token expiration time in seconds. */
    private long expiresIn;

    /**
     * Constructor for creating auth response with tokens and user info.
     *
     * @param accessToken the access token
     * @param refreshToken the refresh token
     * @param user the user profile
     * @param role the user role
     * @param expiresIn the access token expiration time in seconds
     */
    public AuthResponse(
            String accessToken,
            String refreshToken,
            UserProfileResponse user,
            UserRole role,
            long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.role = role;
        this.expiresIn = expiresIn;
        this.tokenType = "Bearer";
    }
}
