package com.horseriding.ecommerce.auth;

import com.horseriding.ecommerce.users.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Enhanced JWT token provider that supports both access tokens and refresh tokens.
 * Handles token creation, parsing, validation, and refresh token management.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    /** Secret key for JWT token signing. */
    @Value("${jwt.secret:defaultSecretKeyForDevelopmentEnvironmentOnly}")
    private String secret;

    /** Access token validity duration in milliseconds (default: 15 minutes). */
    @Value("${jwt.access-token.expiration:900000}")
    private long accessTokenExpiration;

    /** Refresh token validity duration in milliseconds (default: 7 days). */
    @Value("${jwt.refresh-token.expiration:604800000}")
    private long refreshTokenExpiration;

    /** Token type claim key. */
    private static final String TOKEN_TYPE_CLAIM = "token_type";

    /** Access token type. */
    private static final String ACCESS_TOKEN_TYPE = "access";

    /** Refresh token type. */
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    /**
     * Extracts username from JWT token.
     *
     * @param token the JWT token
     * @return the username (email) from the token
     */
    public String extractUsername(final String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts user ID from JWT token.
     *
     * @param token the JWT token
     * @return the user ID from the token
     */
    public Long extractUserId(final String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extracts user role from JWT token.
     *
     * @param token the JWT token
     * @return the user role from the token
     */
    public String extractUserRole(final String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extracts token type from JWT token.
     *
     * @param token the JWT token
     * @return the token type from the token
     */
    public String extractTokenType(final String token) {
        return extractClaim(token, claims -> claims.get(TOKEN_TYPE_CLAIM, String.class));
    }

    /**
     * Extracts expiration date from JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(final String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the JWT token.
     *
     * @param token the JWT token
     * @param claimsResolver function to extract a specific claim
     * @param <T> the type of the claim
     * @return the extracted claim
     */
    public <T> T extractClaim(final String token, final Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extracts all claims from the JWT token.
     *
     * @param token the JWT token
     * @return all claims from the token
     */
    private Claims extractAllClaims(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if the token is expired.
     *
     * @param token the JWT token
     * @return true if token is expired, false otherwise
     */
    public Boolean isTokenExpired(final String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Generates an access token for a user.
     *
     * @param user the user for whom to generate the token
     * @return the generated access token
     */
    public String generateAccessToken(final User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());
        claims.put("name", user.getFullName());
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);
        return createToken(claims, user.getEmail(), accessTokenExpiration);
    }

    /**
     * Generates a refresh token for a user.
     *
     * @param user the user for whom to generate the token
     * @return the generated refresh token
     */
    public String generateRefreshToken(final User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);
        claims.put("jti", UUID.randomUUID().toString()); // JWT ID for uniqueness
        return createToken(claims, user.getEmail(), refreshTokenExpiration);
    }

    /**
     * Creates a JWT token with the specified claims, subject, and expiration.
     *
     * @param claims the claims to include in the token
     * @param subject the subject of the token (typically username/email)
     * @param expiration the expiration time in milliseconds
     * @return the created JWT token
     */
    private String createToken(
            final Map<String, Object> claims, final String subject, final long expiration) {
        Date now = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates an access token for a specific user.
     *
     * @param token the JWT token to validate
     * @param user the user to validate against
     * @return true if token is valid, false otherwise
     */
    public Boolean validateAccessToken(final String token, final User user) {
        try {
            final String username = extractUsername(token);
            final String tokenType = extractTokenType(token);
            return username.equals(user.getEmail())
                    && ACCESS_TOKEN_TYPE.equals(tokenType)
                    && !isTokenExpired(token);
        } catch (JwtException e) {
            log.debug("Access token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates a refresh token.
     *
     * @param token the refresh token to validate
     * @return true if token is valid, false otherwise
     */
    public Boolean validateRefreshToken(final String token) {
        try {
            final String tokenType = extractTokenType(token);
            return REFRESH_TOKEN_TYPE.equals(tokenType) && !isTokenExpired(token);
        } catch (JwtException e) {
            log.debug("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gets the expiration time for refresh tokens.
     *
     * @return the refresh token expiration time as LocalDateTime
     */
    public LocalDateTime getRefreshTokenExpiration() {
        return LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);
    }

    /**
     * Gets the access token expiration time in seconds.
     *
     * @return the access token expiration time in seconds
     */
    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
    }

    /**
     * Gets the signing key for JWT token signing.
     *
     * @return the signing key
     */
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Converts LocalDateTime to Date.
     *
     * @param localDateTime the LocalDateTime to convert
     * @return the converted Date
     */
    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
