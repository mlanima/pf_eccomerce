package com.horseriding.ecommerce.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT authentication filter that validates JWT tokens and sets up Spring Security context.
 * This filter runs once per request and validates the JWT token from the Authorization header.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;

    /**
     * Filters incoming requests to validate JWT tokens and set up authentication context.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Extract JWT token from Authorization header
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // Validate token format and expiration
                if (!jwtTokenProvider.isTokenExpired(jwt)) {
                    // Check if token is blacklisted
                    if (!tokenService.isTokenBlacklisted(jwt)) {
                        // Extract username from token
                        String username = jwtTokenProvider.extractUsername(jwt);

                        // Load user details
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        // Validate token against user
                        if (userDetails instanceof UserPrincipal
                                && jwtTokenProvider.validateAccessToken(
                                        jwt, ((UserPrincipal) userDetails).getUser())) {
                            // Create authentication token
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());

                            // Set authentication details
                            authentication.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(request));

                            // Set authentication in security context
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            log.debug("Successfully authenticated user: {}", username);
                        } else {
                            log.debug("Invalid JWT token for user: {}", username);
                        }
                    } else {
                        log.debug("JWT token is blacklisted");
                    }
                } else {
                    log.debug("JWT token is expired");
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            // Clear security context on error
            SecurityContextHolder.clearContext();
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Determines if this filter should be applied to the given request.
     * Skip authentication for public endpoints.
     *
     * @param request the HTTP request
     * @return true if filter should be skipped, false otherwise
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Skip authentication for public endpoints
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/public/")
                || path.startsWith("/actuator/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/")
                || path.equals("/favicon.ico")
                || path.equals("/error");
    }

    /**
     * Extracts JWT token from the Authorization header.
     *
     * @param request the HTTP request
     * @return the JWT token or null if not found
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
