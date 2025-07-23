package com.horseriding.ecommerce.auth.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for token refresh responses.
 * Contains the new access token and refresh token information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponse {

    /** New access token for API authentication. */
    private String accessToken;

    /** Refresh token (same as provided in request). */
    private String refreshToken;

    /** Token type (always "Bearer"). */
    private String tokenType = "Bearer";

    /** Access token expiration time in seconds. */
    private long expiresIn;

    /**
     * Constructor for creating token refresh response.
     *
     * @param accessToken the new access token
     * @param refreshToken the refresh token
     * @param expiresIn the access token expiration time in seconds
     */
    public TokenRefreshResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenType = "Bearer";
    }
}
