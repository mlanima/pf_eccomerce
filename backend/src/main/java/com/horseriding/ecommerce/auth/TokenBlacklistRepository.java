package com.horseriding.ecommerce.auth;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for TokenBlacklist entity.
 * Provides data access methods for token blacklist management.
 */
@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    /**
     * Check if a token hash exists in the blacklist.
     *
     * @param tokenHash the hash of the token to check
     * @return true if the token is blacklisted, false otherwise
     */
    boolean existsByTokenHash(String tokenHash);

    /**
     * Delete all expired blacklist entries.
     *
     * @param now the current timestamp
     */
    @Modifying
    @Query("DELETE FROM TokenBlacklist tb WHERE tb.expiresAt < :now")
    void deleteExpiredEntries(@Param("now") LocalDateTime now);
}
