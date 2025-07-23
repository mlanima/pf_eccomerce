package com.horseriding.ecommerce.auth.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for token refresh requests.
 * Contains the refresh token needed to obtain a new access token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshRequest {

    /** Refresh token for obtaining new access token. */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
