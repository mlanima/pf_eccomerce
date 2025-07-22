package com.horseriding.ecommerce.auth;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * TokenBlacklist entity for storing blacklisted JWT tokens.
 * Used to invalidate tokens during logout and prevent token reuse.
 */
@Entity
@Table(
        name = "token_blacklist",
        indexes = {
            @Index(name = "idx_token_blacklist_token", columnList = "token_hash", unique = true),
            @Index(name = "idx_token_blacklist_expires_at", columnList = "expires_at")
        })
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    @NotNull(message = "Token hash is required")
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    @NotNull(message = "Expiration date is required")
    private LocalDateTime expiresAt;

    @Column(name = "blacklisted_at", nullable = false, updatable = false)
    private LocalDateTime blacklistedAt;

    /**
     * Constructor for creating new blacklisted tokens.
     *
     * @param tokenHash the hash of the blacklisted token
     * @param expiresAt the expiration date of the original token
     */
    public TokenBlacklist(String tokenHash, LocalDateTime expiresAt) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    /**
     * JPA lifecycle callback executed before persisting the entity.
     */
    @PrePersist
    protected void onCreate() {
        this.blacklistedAt = LocalDateTime.now();
    }

    /**
     * Checks if the blacklist entry is expired and can be cleaned up.
     *
     * @return true if the entry is expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}