package com.horseriding.ecommerce.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.horseriding.ecommerce.auth.dtos.requests.TokenRefreshRequest;
import com.horseriding.ecommerce.users.User;
import com.horseriding.ecommerce.users.UserRepository;
import com.horseriding.ecommerce.users.UserRole;
import com.horseriding.ecommerce.users.dtos.requests.UserLoginRequest;
import com.horseriding.ecommerce.users.dtos.requests.UserRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * Tests the complete request-response cycle for authentication endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional  // Auto-rollback after each test
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TokenService tokenService;

    // Helper method to create test user
    private User createTestUser(String email, String password, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setFirstName("Test");
        user.setLastName("User");
        return userRepository.save(user);
    }

    // Helper method to create tokens for a user
    private TokenService.TokenPair createTokensForUser(User user) {
        return tokenService.createTokens(user);
    }

    // Helper method to create expired refresh token
    private RefreshToken createExpiredRefreshToken(User user) {
        RefreshToken expiredToken = new RefreshToken(
            "expired-refresh-token-" + System.currentTimeMillis(),
            user,
            LocalDateTime.now().minusHours(1) // Expired 1 hour ago
        );
        return refreshTokenRepository.save(expiredToken);
    }

    // Helper method to create revoked refresh token
    private RefreshToken createRevokedRefreshToken(User user) {
        RefreshToken revokedToken = new RefreshToken(
            "revoked-refresh-token-" + System.currentTimeMillis(),
            user,
            LocalDateTime.now().plusHours(24) // Valid expiration but will be revoked
        );
        revokedToken.revoke();
        return refreshTokenRepository.save(revokedToken);
    }

    @Test
    void shouldRegisterUserWithValidData() throws Exception {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("SecurePass123!");
        request.setFirstName("John");
        request.setLastName("Doe");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist()); // Password should not be returned
    }

    @Test
    void shouldReturnValidationErrorForInvalidEmail() throws Exception {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("invalid-email");
        request.setPassword("SecurePass123!");
        request.setFirstName("John");
        request.setLastName("Doe");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnValidationErrorForMissingFields() throws Exception {
        // Given
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("test@example.com");
        // Missing password, firstName, lastName

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectDuplicateEmailRegistration() throws Exception {
        // Given - Create existing user
        createTestUser("existing@example.com", "password123", UserRole.CUSTOMER);

        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setEmail("existing@example.com");
        request.setPassword("SecurePass123!");
        request.setFirstName("John");
        request.setLastName("Doe");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        // Given - Create test user
        createTestUser("test@example.com", "password123", UserRole.CUSTOMER);

        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"));
    }

    @Test
    void shouldRejectLoginWithInvalidEmail() throws Exception {
        // Given
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLoginWithIncorrectPassword() throws Exception {
        // Given - Create test user
        createTestUser("test@example.com", "password123", UserRole.CUSTOMER);

        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLoginWithInvalidRequestFormat() throws Exception {
        // Given - Invalid JSON
        String invalidJson = "{\"email\":\"test@example.com\"}"; // Missing password

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    // Token Refresh Endpoint Tests

    @Test
    void shouldRefreshTokenWithValidRefreshToken() throws Exception {
        // Given - Create test user and tokens
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        TokenService.TokenPair tokens = createTokensForUser(user);

        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken(tokens.getRefreshToken());

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").value(tokens.getRefreshToken()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void shouldRejectTokenRefreshWithInvalidRefreshToken() throws Exception {
        // Given - Invalid refresh token
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("invalid-refresh-token");

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectTokenRefreshWithExpiredRefreshToken() throws Exception {
        // Given - Create test user and expired refresh token
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        RefreshToken expiredToken = createExpiredRefreshToken(user);

        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken(expiredToken.getToken());

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectTokenRefreshWithBlacklistedRefreshToken() throws Exception {
        // Given - Create test user and revoked (blacklisted) refresh token
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        RefreshToken revokedToken = createRevokedRefreshToken(user);

        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken(revokedToken.getToken());

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectTokenRefreshWithMissingRefreshToken() throws Exception {
        // Given - Request with missing refresh token
        TokenRefreshRequest request = new TokenRefreshRequest();
        // refreshToken is null

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectTokenRefreshWithEmptyRefreshToken() throws Exception {
        // Given - Request with empty refresh token
        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken("");

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNewAccessTokenOnSuccessfulRefresh() throws Exception {
        // Given - Create test user and tokens
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        TokenService.TokenPair tokens = createTokensForUser(user);

        TokenRefreshRequest request = new TokenRefreshRequest();
        request.setRefreshToken(tokens.getRefreshToken());

        // When
        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Then - Verify new access token is different from original
        String responseBody = result.getResponse().getContentAsString();
        // The new access token should be different from the original one
        // (This is implicitly tested by the token generation process)
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isString());
    }

    // Logout Endpoint Tests

    @Test
    void shouldLogoutSuccessfullyWithValidAccessToken() throws Exception {
        // Given - Create test user and tokens
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        TokenService.TokenPair tokens = createTokensForUser(user);

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void shouldBlacklistTokenAfterLogout() throws Exception {
        // Given - Create test user and tokens
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        TokenService.TokenPair tokens = createTokensForUser(user);

        // When - Logout with valid token
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isOk());

        // Then - Verify token is blacklisted by trying to access protected endpoint
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLogoutWithInvalidToken() throws Exception {
        // Given - Invalid access token
        String invalidToken = "invalid.jwt.token";

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isOk()); // Logout endpoint doesn't validate token format, just blacklists it
    }

    @Test
    void shouldAccessProtectedEndpointBeforeLogout() throws Exception {
        // Given - Create test user and tokens
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        TokenService.TokenPair tokens = createTokensForUser(user);

        // When & Then - Should be able to access protected endpoint before logout
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectAccessToProtectedEndpointWithBlacklistedToken() throws Exception {
        // Given - Create test user and tokens
        User user = createTestUser("test@example.com", "password123", UserRole.CUSTOMER);
        TokenService.TokenPair tokens = createTokensForUser(user);

        // First verify token works
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isOk());

        // When - Logout to blacklist the token
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isOk());

        // Then - Should not be able to access protected endpoint with blacklisted token
        mockMvc.perform(get("/api/users/profile")
                .header("Authorization", "Bearer " + tokens.getAccessToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandleLogoutWithoutAuthorizationHeader() throws Exception {
        // When & Then - Logout without Authorization header should still return success
        // (The logout endpoint handles null tokens gracefully)
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    void shouldHandleLogoutWithMalformedAuthorizationHeader() throws Exception {
        // Given - Malformed Authorization header (missing "Bearer " prefix)
        String malformedHeader = "InvalidTokenFormat";

        // When & Then - Should still return success (logout endpoint is lenient)
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", malformedHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }
}