package com.horseriding.ecommerce.auth;

import com.horseriding.ecommerce.users.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for RefreshToken entity.
 * Provides data access methods for refresh token management.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find a refresh token by token string.
     *
     * @param token the token string
     * @return Optional containing the refresh token if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all refresh tokens for a specific user.
     *
     * @param user the user
     * @return list of refresh tokens for the user
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Find all valid (non-revoked and non-expired) refresh tokens for a user.
     *
     * @param user the user
     * @param now the current timestamp
     * @return list of valid refresh tokens for the user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Revoke all refresh tokens for a specific user.
     *
     * @param user the user
     * @param revokedAt the revocation timestamp
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :revokedAt WHERE rt.user = :user AND rt.revoked = false")
    void revokeAllTokensByUser(@Param("user") User user, @Param("revokedAt") LocalDateTime revokedAt);

    /**
     * Delete all expired refresh tokens.
     *
     * @param now the current timestamp
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete all revoked refresh tokens older than specified date.
     *
     * @param cutoffDate the cutoff date
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true AND rt.revokedAt < :cutoffDate")
    void deleteRevokedTokensOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Check if a refresh token exists and is valid.
     *
     * @param token the token string
     * @param now the current timestamp
     * @return true if the token exists and is valid
     */
    @Query("SELECT COUNT(rt) > 0 FROM RefreshToken rt WHERE rt.token = :token AND rt.revoked = false AND rt.expiresAt > :now")
    boolean existsValidToken(@Param("token") String token, @Param("now") LocalDateTime now);
}