package com.horseriding.ecommerce.auth;

import com.horseriding.ecommerce.auth.dtos.requests.TokenRefreshRequest;
import com.horseriding.ecommerce.auth.dtos.responses.AuthResponse;
import com.horseriding.ecommerce.auth.dtos.responses.TokenRefreshResponse;
import com.horseriding.ecommerce.common.dtos.responses.SuccessResponse;
import com.horseriding.ecommerce.users.UserService;
import com.horseriding.ecommerce.users.dtos.requests.UserLoginRequest;
import com.horseriding.ecommerce.users.dtos.requests.UserRegistrationRequest;
import com.horseriding.ecommerce.users.dtos.responses.UserProfileResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for authentication endpoints.
 * Handles user registration and login.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /** User service for authentication operations. */
    private final UserService userService;
    
    /** Token service for token management operations. */
    private final TokenService tokenService;
    
    /** JWT token provider for token operations. */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Registers a new user.
     *
     * @param request the registration request
     * @return the created user profile
     */
    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        System.out.println("Hi there!!!");
        UserProfileResponse response = userService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request the login request
     * @return authentication response with JWT token and user profile
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        AuthResponse response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * @param request the token refresh request containing the refresh token
     * @return new access token and refresh token information
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        TokenService.TokenPair tokenPair = tokenService.refreshAccessToken(request.getRefreshToken());
        
        // Get access token expiration in seconds
        long expiresIn = jwtTokenProvider.getAccessTokenExpirationInSeconds();
        
        TokenRefreshResponse response = new TokenRefreshResponse(
                tokenPair.getAccessToken(),
                tokenPair.getRefreshToken(),
                expiresIn
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Logs out a user by blacklisting the access token and revoking the refresh token.
     *
     * @param request the HTTP request containing the Authorization header
     * @return success response
     */
    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String accessToken = null;
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }
        
        // Extract refresh token from request body if provided
        // For now, we'll just handle access token blacklisting
        tokenService.logout(accessToken, null);
        
        SuccessResponse<Void> response = new SuccessResponse<>("Logged out successfully", 200);
        return ResponseEntity.ok(response);
    }
}
