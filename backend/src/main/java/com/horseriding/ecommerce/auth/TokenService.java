package com.horseriding.ecommerce.auth;

import com.horseriding.ecommerce.exception.AuthenticationException;
import com.horseriding.ecommerce.exception.ResourceNotFoundException;
import com.horseriding.ecommerce.users.User;
import com.horseriding.ecommerce.users.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing JWT tokens and refresh tokens.
 * Handles token creation, validation, refresh, and cleanup operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final UserRepository userRepository;

    /**
     * Creates both access and refresh tokens for a user.
     *
     * @param user the user for whom to create tokens
     * @return TokenPair containing both access and refresh tokens
     */
    @Transactional
    public TokenPair createTokens(final User user) {
        // Generate access token
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        
        // Generate refresh token
        String refreshTokenString = jwtTokenProvider.generateRefreshToken(user);
        
        // Save refresh token to database
        RefreshToken refreshToken = new RefreshToken(
                refreshTokenString,
                user,
                jwtTokenProvider.getRefreshTokenExpiration()
        );
        refreshTokenRepository.save(refreshToken);
        
        log.debug("Created tokens for user: {}", user.getEmail());
        
        return new TokenPair(accessToken, refreshTokenString);
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * @param refreshTokenString the refresh token string
     * @return TokenPair containing new access token and the same refresh token
     * @throws AuthenticationException if refresh token is invalid
     */
    @Transactional
    public TokenPair refreshAccessToken(final String refreshTokenString) {
        // Validate refresh token format
        if (!jwtTokenProvider.validateRefreshToken(refreshTokenString)) {
            throw new AuthenticationException("Invalid refresh token format");
        }

        // Find refresh token in database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new AuthenticationException("Refresh token not found"));

        // Check if refresh token is valid
        if (!refreshToken.isValid()) {
            throw new AuthenticationException("Refresh token is expired or revoked");
        }

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(refreshToken.getUser());
        
        log.debug("Refreshed access token for user: {}", refreshToken.getUser().getEmail());
        
        return new TokenPair(newAccessToken, refreshTokenString);
    }

    /**
     * Revokes a specific refresh token.
     *
     * @param refreshTokenString the refresh token to revoke
     */
    @Transactional
    public void revokeRefreshToken(final String refreshTokenString) {
        refreshTokenRepository.findByToken(refreshTokenString)
                .ifPresent(refreshToken -> {
                    refreshToken.revoke();
                    refreshTokenRepository.save(refreshToken);
                    log.debug("Revoked refresh token for user: {}", refreshToken.getUser().getEmail());
                });
    }

    /**
     * Revokes all refresh tokens for a specific user.
     *
     * @param user the user whose tokens should be revoked
     */
    @Transactional
    public void revokeAllUserTokens(final User user) {
        refreshTokenRepository.revokeAllTokensByUser(user, LocalDateTime.now());
        log.debug("Revoked all refresh tokens for user: {}", user.getEmail());
    }

    /**
     * Revokes all refresh tokens for a specific user by user ID.
     *
     * @param userId the ID of the user whose tokens should be revoked
     */
    @Transactional
    public void revokeAllUserTokens(final Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        revokeAllUserTokens(user);
    }

    /**
     * Gets all valid refresh tokens for a user.
     *
     * @param user the user
     * @return list of valid refresh tokens
     */
    @Transactional(readOnly = true)
    public List<RefreshToken> getValidUserTokens(final User user) {
        return refreshTokenRepository.findValidTokensByUser(user, LocalDateTime.now());
    }

    /**
     * Checks if a refresh token is valid.
     *
     * @param refreshTokenString the refresh token to check
     * @return true if the token is valid, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isRefreshTokenValid(final String refreshTokenString) {
        return refreshTokenRepository.existsValidToken(refreshTokenString, LocalDateTime.now());
    }

    /**
     * Extracts user from access token.
     *
     * @param accessToken the access token
     * @return the user associated with the token
     * @throws AuthenticationException if token is invalid or user not found
     */
    @Transactional(readOnly = true)
    public User getUserFromAccessToken(final String accessToken) {
        try {
            String email = jwtTokenProvider.extractUsername(accessToken);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new AuthenticationException("User not found"));
        } catch (Exception e) {
            throw new AuthenticationException("Invalid access token");
        }
    }

    /**
     * Validates an access token for a specific user.
     *
     * @param accessToken the access token to validate
     * @param user the user to validate against
     * @return true if token is valid, false otherwise
     */
    public boolean validateAccessToken(final String accessToken, final User user) {
        // Check if token is blacklisted first
        if (isTokenBlacklisted(accessToken)) {
            return false;
        }
        return jwtTokenProvider.validateAccessToken(accessToken, user);
    }

    /**
     * Blacklists an access token (used for logout).
     *
     * @param accessToken the access token to blacklist
     */
    @Transactional
    public void blacklistToken(final String accessToken) {
        try {
            // Get token expiration from JWT
            Date expiration = jwtTokenProvider.extractExpiration(accessToken);
            LocalDateTime expiresAt = expiration.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            
            // Create hash of the token for storage
            String tokenHash = hashToken(accessToken);
            
            // Save to blacklist
            TokenBlacklist blacklistEntry = new TokenBlacklist(tokenHash, expiresAt);
            tokenBlacklistRepository.save(blacklistEntry);
            
            log.debug("Blacklisted access token");
        } catch (Exception e) {
            log.warn("Failed to blacklist token: {}", e.getMessage());
            // Don't throw exception as this is not critical for logout flow
        }
    }

    /**
     * Checks if an access token is blacklisted.
     *
     * @param accessToken the access token to check
     * @return true if the token is blacklisted, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isTokenBlacklisted(final String accessToken) {
        try {
            String tokenHash = hashToken(accessToken);
            return tokenBlacklistRepository.existsByTokenHash(tokenHash);
        } catch (Exception e) {
            log.warn("Failed to check token blacklist status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Performs logout by blacklisting access token and revoking refresh token.
     *
     * @param accessToken the access token to blacklist
     * @param refreshToken the refresh token to revoke (optional)
     */
    @Transactional
    public void logout(final String accessToken, final String refreshToken) {
        // Blacklist the access token
        blacklistToken(accessToken);
        
        // Revoke the refresh token if provided
        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            revokeRefreshToken(refreshToken);
        }
        
        log.debug("User logged out successfully");
    }

    /**
     * Creates a SHA-256 hash of a token for storage in blacklist.
     *
     * @param token the token to hash
     * @return the SHA-256 hash of the token
     */
    private String hashToken(final String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Scheduled task to clean up expired and revoked tokens.
     * Runs every hour to maintain database cleanliness.
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        
        // Delete expired refresh tokens
        refreshTokenRepository.deleteExpiredTokens(now);
        
        // Delete revoked refresh tokens older than 30 days
        LocalDateTime cutoffDate = now.minusDays(30);
        refreshTokenRepository.deleteRevokedTokensOlderThan(cutoffDate);
        
        // Delete expired blacklist entries
        tokenBlacklistRepository.deleteExpiredEntries(now);
        
        log.debug("Cleaned up expired and old revoked refresh tokens and blacklist entries");
    }

    /**
     * Data class representing a pair of access and refresh tokens.
     */
    public static class TokenPair {
        private final String accessToken;
        private final String refreshToken;

        public TokenPair(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}